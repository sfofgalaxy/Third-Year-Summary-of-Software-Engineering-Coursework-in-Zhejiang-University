// SPDX-License-Identifier: GPL-2.0
/*
 *  linux/fs/myext2/file.c
 *
 * Copyright (C) 1992, 1993, 1994, 1995
 * Remy Card (card@masi.ibp.fr)
 * Laboratoire MASI - Institut Blaise Pascal
 * Universite Pierre et Marie Curie (Paris VI)
 *
 *  from
 *
 *  linux/fs/minix/file.c
 *
 *  Copyright (C) 1991, 1992  Linus Torvalds
 *
 *  myext2 fs regular file handling primitives
 *
 *  64-bit file support on 64-bit platforms by Jakub Jelinek
 * 	(jj@sunsite.ms.mff.cuni.cz)
 */

#include <linux/time.h>
#include <linux/pagemap.h>
#include <linux/dax.h>
#include <linux/quotaops.h>
#include <linux/iomap.h>
#include <linux/uio.h>
#include "myext2.h"
#include "xattr.h"
#include "acl.h"

#ifdef CONFIG_FS_DAX

static ssize_t myext2_dax_read_iter(struct kiocb *iocb, struct iov_iter *to)
{
	struct inode *inode = iocb->ki_filp->f_mapping->host;
	ssize_t ret;

	if (!iov_iter_count(to))
		return 0; /* skip atime */

	inode_lock_shared(inode);
	ret = dax_iomap_rw(iocb, to, &myext2_iomap_ops);
	inode_unlock_shared(inode);

	file_accessed(iocb->ki_filp);
	return ret;
}

static ssize_t new_sync_read(struct file *filp, char __user *buf, size_t len, loff_t *ppos)
{
	struct iovec iov = { .iov_base = buf, .iov_len = len };
	struct kiocb kiocb;
	struct iov_iter iter;
	ssize_t ret;

	init_sync_kiocb(&kiocb, filp);
	kiocb.ki_pos = *ppos;
	iov_iter_init(&iter, READ, &iov, 1, len);

	ret = call_read_iter(filp, &kiocb, &iter);
	BUG_ON(ret == -EIOCBQUEUED);
	*ppos = kiocb.ki_pos;
	return ret;
}

static ssize_t new_sync_write(struct file *filp, const char __user *buf, size_t len, loff_t *ppos)
{
	struct iovec iov = { .iov_base = (void __user *)buf, .iov_len = len };
	struct kiocb kiocb;
	struct iov_iter iter;
	ssize_t ret;

	init_sync_kiocb(&kiocb, filp);
	kiocb.ki_pos = *ppos;
	iov_iter_init(&iter, WRITE, &iov, 1, len);

	ret = call_write_iter(filp, &kiocb, &iter);
	BUG_ON(ret == -EIOCBQUEUED);
	if (ret > 0)
		*ppos = kiocb.ki_pos;
	return ret;
}
#define KEY	0x9E
static ssize_t new_sync_read_crypt(struct file *filp, char __user *buf, size_t len, loff_t *ppos) {
	int i = 0;
	ssize_t ret = new_sync_read(filp, buf, len, ppos);
	for(i = 0; i < len; i++) {
		char* x = (char*)kmalloc(sizeof(char), GFP_ATOMIC);
		copy_from_user(x, buf + i, 1); 
		x[0] ^= KEY;
		copy_to_user(buf + i, x, 1);
	}
	printk("[*] read: haha decrypt %ld \n", len);
	return ret;
}
static ssize_t new_sync_write_crypt(struct file *filp, const char __user *buf, size_t len, loff_t *ppos) {
	int i = 0;
	for(i = 0; i < len; i++) {
		char* x = (char*)kmalloc(sizeof(char), GFP_ATOMIC);
		copy_from_user(x, buf + i, 1); 
		x[0] ^= KEY;
		copy_to_user(buf + i, x, 1);
	}
	printk("[*] write: haha encrypt %ld \n", len);
	ssize_t ret = new_sync_write(filp, buf, len, ppos);
	return ret;
}

static ssize_t myext2_dax_write_iter(struct kiocb *iocb, struct iov_iter *from)
{
	struct file *file = iocb->ki_filp;
	struct inode *inode = file->f_mapping->host;
	ssize_t ret;

	inode_lock(inode);
	ret = generic_write_checks(iocb, from);
	if (ret <= 0)
		goto out_unlock;
	ret = file_remove_privs(file);
	if (ret)
		goto out_unlock;
	ret = file_update_time(file);
	if (ret)
		goto out_unlock;

	ret = dax_iomap_rw(iocb, from, &myext2_iomap_ops);
	if (ret > 0 && iocb->ki_pos > i_size_read(inode)) {
		i_size_write(inode, iocb->ki_pos);
		mark_inode_dirty(inode);
	}

out_unlock:
	inode_unlock(inode);
	if (ret > 0)
		ret = generic_write_sync(iocb, ret);
	return ret;
}

/*
 * The lock ordering for myext2 DAX fault paths is:
 *
 * mmap_sem (MM)
 *   sb_start_pagefault (vfs, freeze)
 *     myext2_inode_info->dax_sem
 *       address_space->i_mmap_rwsem or page_lock (mutually exclusive in DAX)
 *         myext2_inode_info->truncate_mutex
 *
 * The default page_lock and i_size verification done by non-DAX fault paths
 * is sufficient because myext2 doesn't support hole punching.
 */
static vm_fault_t myext2_dax_fault(struct vm_fault *vmf)
{
	struct inode *inode = file_inode(vmf->vma->vm_file);
	struct myext2_inode_info *ei = MYEXT2_I(inode);
	vm_fault_t ret;

	if (vmf->flags & FAULT_FLAG_WRITE) {
		sb_start_pagefault(inode->i_sb);
		file_update_time(vmf->vma->vm_file);
	}
	down_read(&ei->dax_sem);

	ret = dax_iomap_fault(vmf, PE_SIZE_PTE, NULL, NULL, &myext2_iomap_ops);

	up_read(&ei->dax_sem);
	if (vmf->flags & FAULT_FLAG_WRITE)
		sb_end_pagefault(inode->i_sb);
	return ret;
}

static const struct vm_operations_struct myext2_dax_vm_ops = {
	.fault		= myext2_dax_fault,
	/*
	 * .huge_fault is not supported for DAX because allocation in myext2
	 * cannot be reliably aligned to huge page sizes and so pmd faults
	 * will always fail and fail back to regular faults.
	 */
	.page_mkwrite	= myext2_dax_fault,
	.pfn_mkwrite	= myext2_dax_fault,
};

static int myext2_file_mmap(struct file *file, struct vm_area_struct *vma)
{
	if (!IS_DAX(file_inode(file)))
		return generic_file_mmap(file, vma);

	file_accessed(file);
	vma->vm_ops = &myext2_dax_vm_ops;
	return 0;
}
#else
#define myext2_file_mmap	generic_file_mmap
#endif

/*
 * Called when filp is released. This happens when all file descriptors
 * for a single struct file are closed. Note that different open() calls
 * for the same file yield different struct file structures.
 */
static int myext2_release_file (struct inode * inode, struct file * filp)
{
	if (filp->f_mode & FMODE_WRITE) {
		mutex_lock(&MYEXT2_I(inode)->truncate_mutex);
		myext2_discard_reservation(inode);
		mutex_unlock(&MYEXT2_I(inode)->truncate_mutex);
	}
	return 0;
}

int myext2_fsync(struct file *file, loff_t start, loff_t end, int datasync)
{
	int ret;
	struct super_block *sb = file->f_mapping->host->i_sb;

	ret = generic_file_fsync(file, start, end, datasync);
	if (ret == -EIO)
		/* We don't really know where the IO error happened... */
		myext2_error(sb, __func__,
			   "detected IO error when writing metadata buffers");
	return ret;
}

static ssize_t myext2_file_read_iter(struct kiocb *iocb, struct iov_iter *to)
{
#ifdef CONFIG_FS_DAX
	if (IS_DAX(iocb->ki_filp->f_mapping->host))
		return myext2_dax_read_iter(iocb, to);
#endif
	return generic_file_read_iter(iocb, to);
}

static ssize_t myext2_file_write_iter(struct kiocb *iocb, struct iov_iter *from)
{
#ifdef CONFIG_FS_DAX
	if (IS_DAX(iocb->ki_filp->f_mapping->host))
		return myext2_dax_write_iter(iocb, from);
#endif
	return generic_file_write_iter(iocb, from);
}

const struct file_operations myext2_file_operations = {
	.llseek		= generic_file_llseek,
	.read		= new_sync_read_crypt,
	.write		= new_sync_write_crypt,
	.read_iter	= myext2_file_read_iter,
	.write_iter	= myext2_file_write_iter,
	.unlocked_ioctl = myext2_ioctl,
#ifdef CONFIG_COMPAT
	.compat_ioctl	= myext2_compat_ioctl,
#endif
	.mmap		= myext2_file_mmap,
	.open		= dquot_file_open,
	.release	= myext2_release_file,
	.fsync		= myext2_fsync,
	.get_unmapped_area = thp_get_unmapped_area,
	.splice_read	= generic_file_splice_read,
	.splice_write	= iter_file_splice_write,
};

const struct inode_operations myext2_file_inode_operations = {
#ifdef CONFIG_MYEXT2_FS_XATTR
	.listxattr	= myext2_listxattr,
#endif
	.setattr	= myext2_setattr,
	.get_acl	= myext2_get_acl,
	.set_acl	= myext2_set_acl,
	.fiemap		= myext2_fiemap,
};

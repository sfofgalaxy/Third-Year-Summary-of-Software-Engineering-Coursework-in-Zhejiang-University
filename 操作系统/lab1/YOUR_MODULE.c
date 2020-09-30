#include<linux/module.h>
#include<linux/sched/cputime.h>
#include<linux/sched.h>
#include<linux/init_task.h>
#include<linux/init.h>


static void process_print(struct task_struct *tsk){
		printk(KERN_ALERT "the process pid: %d and the threads are as follow:\n",(int)(tsk->pid));
		return;
}
	
static void thread_print(struct task_struct *tsk){
	struct task_struct *current_thread=NULL;
	struct list_head *current_list = NULL;
	struct list_head *start_list = NULL;
	unsigned long offset;
	offset = offsetof(struct task_struct, thread_group);
	current_list = &(tsk->thread_group);
	start_list = &(tsk->thread_group);
	do{
		current_thread = (unsigned long)current_list - offset;
		printk(KERN_ALERT "pid:%d\ttgid:%d\tstack:%p\ttask_struct:%p\tmm_struct:%p\tname:%s\t\n",(int)(current_thread->pid), (int)(current_thread->tgid), current_thread->stack, current_thread, current_thread->mm, current_thread->comm);
		current_list = current_list->next;
	}while(current_list!=start_list);
	return;
}

static int YOUR_INIT(void){
	//add code, output all processes and thread information
	struct task_struct *tsk;
	tsk = NULL;
	tsk = &init_task;
	for_each_process(tsk){
		process_print(tsk);//output the task;
		thread_print(tsk);//output the threads
	}
}


static void YOUR_EXIT(void){
	printk(KERN_ALERT " exit\n");
}

module_init(YOUR_INIT);

module_exit(YOUR_EXIT);

MODULE_LICENSE("GPL");

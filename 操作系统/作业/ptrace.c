#include <sys/ptrace.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/user.h>
#include <syscall.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/reg.h>

#if __WORDSIZE == 64
#define REG(reg) reg.orig_rax
#else
#define REG(reg) reg.orig_eax
#endif

const int long_size = sizeof(long);

int main(int argc, char* argv[]) {
	pid_t child;
	long orig_rax;
	struct user_regs_struct regs;
	if (argc == 1) {
		exit(0);
	}
	char* chargs[argc];
	int i = 0;
	while (i < argc -1) {
		chargs[i] = argv[i+1];
		i++;
	}
	chargs[i] = NULL;
	child = fork();
	if(child == 0) {
		ptrace(PTRACE_TRACEME, 0, NULL, NULL);
		execvp(chargs[0], chargs);
	} else {
	int status;
	int insyscall = 0;
	long arm;
//	fprintf(stderr,"%d",SYS_open);
	while(waitpid(child, &status, 0) && ! WIFEXITED(status)) {
		orig_rax = ptrace(PTRACE_PEEKUSER,child,8*ORIG_RAX,NULL);
		if(orig_rax==SYS_openat){
			if(insyscall==0){
			insyscall = 1;
			char *str;
			char *add;
			int i,j,end=0;
			union u{
				long value;
				char chars[long_size];
			}da;
			str = calloc((3000)*sizeof(char),sizeof(char));
			add = str;
			i = 0;
			j = 0;
			arm = ptrace(PTRACE_PEEKUSER,child,8*RSI,NULL);
			for(i=0;i>=0;i++)
			{
				da.value = ptrace(PTRACE_PEEKDATA,child,arm+i*8,NULL);
				i++;
				for(j=0;j<long_size;j++)
				{
					if(da.chars[j]=='\0'){
						end=1;
						break;
					}
				}
				if(end==1)
				{
					memcpy(add,da.chars,j+1);
					break;
				}
				else
				{
					memcpy(add,da.chars,long_size);
					add+=long_size;
				}
			}
			fprintf(stderr,"trigger the 'open' system call,panthname is : %s \n",str);
			}
			else
			{
			insyscall = 0;	
			}
			
		}
		ptrace(PTRACE_SYSCALL, child, NULL, NULL);
		}
	}
	return 0;
}

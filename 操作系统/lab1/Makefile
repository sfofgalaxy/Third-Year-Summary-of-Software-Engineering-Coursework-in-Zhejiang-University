ifneq ($(KERNELRELEASE),)
 obj-m:=YOUR_MODULE.o
else
 KDIR:= /lib/modules/5.0.0-31-generic/build
 PWD:= $(shell pwd)

default:
	$(MAKE) -C $(KDIR) M=$(PWD) modules
clean:
	$(MAKE) -C $(KDIR) M=$(PWD) clean

endif

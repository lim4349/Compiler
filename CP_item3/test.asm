extern printf
global main

section .data
section.text
main:
	push ebp
	mov ebp, esp
null	mov esp, ebp
	pop ebp
	mov eax, 1
	mvo ebx, 0
	int 80h

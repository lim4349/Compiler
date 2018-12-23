extern printf
global main

section .data
section.text
main:
	push ebp
	mov ebp, esp
	mov dword [ebp-0x8], 0x0
	mov dword [ebp-0x4], 0x19a
	mov esp, ebp
	pop ebp
	mov eax, 1
	mvo ebx, 0
	int 80h

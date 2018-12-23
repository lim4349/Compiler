extern printf
global main

section .data
	message1 db "%d", 10, 0
section .text
main:
	push ebp
	mov ebp, esp
	sub esp, 0x8
	mov dword [ebp-0x8], 0x0
	mov dword [ebp-0x4], 0x19a
L1:
	cmp dword [ebp-0x4], 0x0
	jle L2
	mov eax, dword [ebp-0x4]
	sar eax, 1
	mov dword [ebp-0x4], eax
	mov eax, dword [ebp-0x8]
	mov ebx, dword [ebp-0x4]
	add eax, ebx
	mov dword [ebp-0x8], eax
	jmp L1
L2:
	mov eax, dword [ebp-0x8]
	push eax
	push dword message1
	call printf
	mov esp, ebp
	pop ebp
	mov eax, 1
	mov ebx, 0
	int 80h

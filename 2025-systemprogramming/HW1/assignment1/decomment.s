	.file	"decomment.c"
	.text
	.section	.rodata
.LC0:
	.string	"./src/decomment.c"
.LC1:
	.string	"0"
	.align 8
.LC2:
	.string	"Error: line %d: unterminated comment\n"
	.text
	.globl	main
	.type	main, @function
main:
.LFB0:
	.cfi_startproc
	endbr64
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$32, %rsp
	movl	$8, -8(%rbp)
	movl	$1, -16(%rbp)
	movl	$-1, -12(%rbp)
.L35:
	call	getchar@PLT
	movl	%eax, -4(%rbp)
	cmpl	$-1, -4(%rbp)
	je	.L44
	movl	-4(%rbp), %eax
	movb	%al, -17(%rbp)
	cmpl	$8, -8(%rbp)
	ja	.L4
	movl	-8(%rbp), %eax
	leaq	0(,%rax,4), %rdx
	leaq	.L6(%rip), %rax
	movl	(%rdx,%rax), %eax
	cltq
	leaq	.L6(%rip), %rdx
	addq	%rdx, %rax
	notrack jmp	*%rax
	.section	.rodata
	.align 4
	.align 4
.L6:
	.long	.L14-.L6
	.long	.L13-.L6
	.long	.L12-.L6
	.long	.L11-.L6
	.long	.L10-.L6
	.long	.L9-.L6
	.long	.L8-.L6
	.long	.L7-.L6
	.long	.L5-.L6
	.text
.L5:
	cmpb	$39, -17(%rbp)
	jne	.L15
	movl	$0, -8(%rbp)
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	jmp	.L19
.L15:
	cmpb	$34, -17(%rbp)
	jne	.L17
	movl	$2, -8(%rbp)
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	jmp	.L19
.L17:
	cmpb	$47, -17(%rbp)
	jne	.L18
	movl	$6, -8(%rbp)
	jmp	.L19
.L18:
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	jmp	.L19
.L14:
	cmpb	$39, -17(%rbp)
	jne	.L20
	movl	$8, -8(%rbp)
	jmp	.L21
.L20:
	cmpb	$92, -17(%rbp)
	jne	.L21
	movl	$1, -8(%rbp)
.L21:
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	jmp	.L19
.L13:
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	movl	$0, -8(%rbp)
	jmp	.L19
.L12:
	cmpb	$34, -17(%rbp)
	jne	.L22
	movl	$8, -8(%rbp)
	jmp	.L23
.L22:
	cmpb	$92, -17(%rbp)
	jne	.L23
	movl	$3, -8(%rbp)
.L23:
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	jmp	.L19
.L11:
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	movl	$2, -8(%rbp)
	jmp	.L19
.L8:
	cmpb	$47, -17(%rbp)
	jne	.L24
	movl	$4, -8(%rbp)
	movl	$32, %edi
	call	putchar@PLT
	jmp	.L19
.L24:
	cmpb	$42, -17(%rbp)
	jne	.L26
	movl	$5, -8(%rbp)
	movl	-16(%rbp), %eax
	movl	%eax, -12(%rbp)
	movl	$32, %edi
	call	putchar@PLT
	jmp	.L19
.L26:
	movl	$8, -8(%rbp)
	movl	$47, %edi
	call	putchar@PLT
	movsbl	-17(%rbp), %eax
	movl	%eax, %edi
	call	putchar@PLT
	jmp	.L19
.L10:
	cmpb	$10, -17(%rbp)
	jne	.L45
	movl	$8, -8(%rbp)
	movl	$10, %edi
	call	putchar@PLT
	jmp	.L45
.L9:
	cmpb	$42, -17(%rbp)
	jne	.L28
	movl	$7, -8(%rbp)
	jmp	.L46
.L28:
	cmpb	$10, -17(%rbp)
	jne	.L46
	movl	$10, %edi
	call	putchar@PLT
	jmp	.L46
.L7:
	cmpb	$47, -17(%rbp)
	jne	.L30
	movl	$8, -8(%rbp)
	jmp	.L19
.L30:
	cmpb	$42, -17(%rbp)
	jne	.L32
	movl	$7, -8(%rbp)
	jmp	.L19
.L32:
	cmpb	$10, -17(%rbp)
	jne	.L33
	movl	$10, %edi
	call	putchar@PLT
	movl	$5, -8(%rbp)
	jmp	.L19
.L33:
	movl	$5, -8(%rbp)
	jmp	.L19
.L4:
	leaq	__PRETTY_FUNCTION__.0(%rip), %rax
	movq	%rax, %rcx
	movl	$145, %edx
	leaq	.LC0(%rip), %rax
	movq	%rax, %rsi
	leaq	.LC1(%rip), %rax
	movq	%rax, %rdi
	call	__assert_fail@PLT
.L45:
	nop
	jmp	.L19
.L46:
	nop
.L19:
	cmpb	$10, -17(%rbp)
	jne	.L35
	addl	$1, -16(%rbp)
	jmp	.L35
.L44:
	nop
	cmpl	$-1, -4(%rbp)
	jne	.L36
	cmpl	$8, -8(%rbp)
	ja	.L37
	movl	-8(%rbp), %eax
	leaq	0(,%rax,4), %rdx
	leaq	.L39(%rip), %rax
	movl	(%rdx,%rax), %eax
	cltq
	leaq	.L39(%rip), %rdx
	addq	%rdx, %rax
	notrack jmp	*%rax
	.section	.rodata
	.align 4
	.align 4
.L39:
	.long	.L47-.L39
	.long	.L47-.L39
	.long	.L47-.L39
	.long	.L47-.L39
	.long	.L47-.L39
	.long	.L40-.L39
	.long	.L41-.L39
	.long	.L40-.L39
	.long	.L47-.L39
	.text
.L40:
	movq	stderr(%rip), %rax
	movl	-12(%rbp), %edx
	leaq	.LC2(%rip), %rcx
	movq	%rcx, %rsi
	movq	%rax, %rdi
	movl	$0, %eax
	call	fprintf@PLT
	movl	$1, %eax
	jmp	.L42
.L41:
	movl	$47, %edi
	call	putchar@PLT
	jmp	.L36
.L37:
	leaq	__PRETTY_FUNCTION__.0(%rip), %rax
	movq	%rax, %rcx
	movl	$179, %edx
	leaq	.LC0(%rip), %rax
	movq	%rax, %rsi
	leaq	.LC1(%rip), %rax
	movq	%rax, %rdi
	call	__assert_fail@PLT
.L47:
	nop
.L36:
	movl	$0, %eax
.L42:
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE0:
	.size	main, .-main
	.section	.rodata
	.type	__PRETTY_FUNCTION__.0, @object
	.size	__PRETTY_FUNCTION__.0, 5
__PRETTY_FUNCTION__.0:
	.string	"main"
	.ident	"GCC: (Ubuntu 13.3.0-6ubuntu2~24.04) 13.3.0"
	.section	.note.GNU-stack,"",@progbits
	.section	.note.gnu.property,"a"
	.align 8
	.long	1f - 0f
	.long	4f - 1f
	.long	5
0:
	.string	"GNU"
1:
	.align 8
	.long	0xc0000002
	.long	3f - 2f
2:
	.long	0x3
3:
	.align 8
4:

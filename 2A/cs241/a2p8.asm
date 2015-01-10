
; $1 holds the address of the beginning of the array
; $2 holds the number of elements in the array
; each element is 3 addresses away from previous
; $6 is the temp height
; $7 is the left branch
; $8 is the right branch
; $10 is the multiplier 4
; $11 is the number 12
; $3 max height saved so far
; $12 used to store -4
lis $27
.word 48
lis $26
.word 49
lis $25
.word 50
lis $23
.word 51
lis $18
.word 0xffff000c
lis $13
.word leftpresent
lis $14
.word rightpresent
lis $11
.word -12
lis $10
.word 4
lis $9
.word -1
lis $12
.word -4
lis $24
.word 3
bne $2, $0, 2; return if element count is 0
add $3, $0, $0
jr $31

bne $24, $2, 2 ; return 1 if element count is 3
sub $3, $0, $9;
jr $31

sub $6, $0, $9; set temp height to 1
sub $3, $0, $9; set max height to 1

lw $7, 4($1) ; position of left branch in array
mult $7, $10 ; address of left branch
mflo $7 ; put the shift of left branch in $7
lw $8, 8($1) ; position of right branch in array
mult $8, $10 ; address of right branch
mflo $8 ; put the shift of right branch in $8

recurse:
;sw $27, 0($18)

beq $7, $8, return ; if they are both -1, so empty, return to calling function
beq $12, $7, end1 ; skip going left if empty
;sw $26, 0($18)
sw $31, -4($30) ; store old return address
sub $6, $6, $9 ; increment temp height
jalr $13
beq $0, $0, checkmax1
backfromcheck1:
lw $31, 8($30) ; after returning, load correct return address
sub $30, $30, $11 ; readjust TOS now that popd 3
end1:
beq $12, $8, end2 ; skip if going right empty
;sw $25, 0($18)
sw $31, -4($30)
sub $6, $6, $9 ; increment temp height
jalr $14
beq $0, $0, checkmax2
backfromcheck2:
lw $31, 8($30) ; after returning load correct return address
sub $30, $30, $11 ; readjust TOS now that popd 3
end2:
jr $31

return:
;sw $23, 0($18)
lw $8, 0($30)
lw $7, 4($30)
jr $31

checkmax1:
slt $21, $3, $6 ; if max is less than temp max
beq $21, $0, 1 ; if $21 is 0, so max is not less than temp max, don't set max to temp
add $3, $0, $6
add $6, $6, $9 ; decrement temp height
beq $0, $0, backfromcheck1

checkmax2:
slt $21, $3, $6 ; if max is less than temp max
beq $21, $0, 1 ; if $21 is 0, so max is not less than temp max, don't set max to temp
add $3, $0, $6
add $6, $6, $9 ; decrement temp height
beq $0, $0, backfromcheck2


leftpresent:
sw $7, -8($30)
sw $8, -12($30) ; store old ones for recursing callbacks
add $30, $30, $11 ; advance TOS 3 places
add $7, $1, $7 ; $7 now holds the proper address 
add $8, $1, $8 ; $8 now holds the proper address
add $20, $0, $7
lw $7, 4($20) ; holds index value of left branch in array
mult $7, $10
mflo $7 ; now contains the number to move from $1 to get to next element
lw $8, 8($20) ; holds index value of right branch in array
mult $8, $10
mflo $8 ; number to move from $1 to get the right address
beq $0, $0, recurse

rightpresent:
sw $7, -8($30)
sw $8, -12($30)
add $30, $30, $11 ; advance TOS 3 places
add $7, $1, $7 ; now holds the proper address
add $8, $1, $8 ; now holds the proper address
add $20, $0, $8
lw $7, 4($20) ; holds index value of left branch in array
mult $7, $10
mflo $7 ; now contains the number to move from $1 to get to left element
lw $8, 8($20)
mult $8, $10
mflo $8 ; now contains the number to move from $1 to get to right element
beq $0, $0, recurse

# cpu_slow

Not a slow CPU

set = 1*

1dddddxxxxxxxxxxxxxxxxxxxxxxxxxx set register = d, value = x

alu = 01*[s][I]

(Signed is s is 1 otherwise unsigned)
(Use b and i as immedidate value)

alu op = t, dest = dsource 1 = a, source 2 = b, immediate = bi (14 bits)

01ttttsIdddddaaaaabbbbbiiiiiiiii 

jumps = 001*[f], register = d, address = x

when f = 1, use register, when 0 use constant

001001fddddd----xxxxxxxxxxxxxxxx jmp equal
001010fddddd----xxxxxxxxxxxxxxxx jmp greater
001100fddddd----xxxxxxxxxxxxxxxx jmp less than
001011fddddd----xxxxxxxxxxxxxxxx jmp geater or equal
001101fddddd----xxxxxxxxxxxxxxxx jmp less than or equal
001110fddddd----xxxxxxxxxxxxxxxx jmp not equal
001111fddddd----xxxxxxxxxxxxxxxx jmp

load/store (ram) = 0001*[f], dest = d, src = s, address = x

src is used when f flag is set
otherwise const address is used.

00010fdddddsssssxxxxxxxxxxxxxxxx load
00011fdddddsssssxxxxxxxxxxxxxxxx store

test a, is_signed = s

000010s-ddddd-------------------

cmp a, b, is_signed = s, immediate= I, immediate value = bi (19 bit)

000011sIaaaaabbbbbiiiiiiiiiiiiii

halt

00000000000000000000000000000001

===========================================

ALU ops

0 = ~ (bitwise not)
1 = & (bitwise and)
2 = | (bitwise or)
3 = ^ (bitwise xor)
4 = + (integer)
5 = - (integer)
6 = / (unsigned)
7 = % (unsigned)
8 = *
9 = >> (shift right)
10 = << (shift left)
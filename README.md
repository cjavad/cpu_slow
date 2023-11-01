# cpu_slow
Not a slow CPU

set = 1*

1dddddxxxxxxxxxxxxxxxxxxxxxxxxxx    set register = d, value = x

alu = 01*

01iiiiiidddddaaaaabbbbb--------    alu op = i, dest = dsource 1 = a, source 2 = b


jumps = 001*[f], register = d, address = x

when f = 1, use register, when 0 use constant

001001fddddd----xxxxxxxxxxxxxxxx    jmp equal
001010fddddd----xxxxxxxxxxxxxxxx    jmp greater
001100fddddd----xxxxxxxxxxxxxxxx    jmp less than
001011fddddd----xxxxxxxxxxxxxxxx    jmp geater or equal
001101fddddd----xxxxxxxxxxxxxxxx    jmp less than or equal
001110fddddd----xxxxxxxxxxxxxxxx    jmp not equal
001111fddddd----xxxxxxxxxxxxxxxx    jmp

load/store (ram) = 0001*, dest = d, address = x

00010ddddd------xxxxxxxxxxxxxxxx    load
00011ddddd------xxxxxxxxxxxxxxxx    store

test a

000010ddddd---------------------

cmp a, b

000011aaaaabbbbb----------------

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
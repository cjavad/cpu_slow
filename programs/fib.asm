; Program that calculates the fibonazzi series up to a certain amount

; Labels store memory sequentially for now, so we cannot reference this directly
; but we know it is stored at memory location 0
.fib_n 35

LOAD r30 0 ; Load target N into reg 30
SET  r0  0 ; F_0
SET  r1  1 ; F_1
SET  r2  0 ; F_n
SET  r3  1 ; index counter we start at 1

start:
    ADD r2 r1 r0  ; F_n = F_n - 1 + F_n - 2
    OR  r0 r1 r1  ; MOV r1 into r0
    OR  r1 r2 r2  ; MOV r2 into r1
    ADD r3 r3 1   ; Increment index by 1
    CMP r3 r30    ; Compare index with target
    JL  start      ; r3 < r30 then jump


end:
    ; For debugging store counters.
    STORE r3  2
    STORE r30 3

    STORE r1 1 ; Write latest result to memory location 1
    HALT

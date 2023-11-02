SET r0 0    ; For loop counter

main:
STORE r0 0  ; Store counter in memory
STORE r0 r0 ; Store rolling sum in memory at i index
ADD r0 r0 1 ; increment with 1
CMP r0 50   ; compare with 50
JLE main    ; if equal, jump to main
HALT        ; end program
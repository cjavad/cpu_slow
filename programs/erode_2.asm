; Erode program

SET r30 0 ; constant 0
SET r31 255 ; constant 255
SET r15 0 ; Y counter

LOOP_Y:
    SET r16 0 ; X counter
    ; Base y * 20
    MUL r18 r15 20
    ; Output base 400 + y * 20
    ADD r18 r18 400

LOOP_X:
    OR r5 r15 r15 ; Y
    OR r6 r16 r16
    ; Output base 400 + y * 20 + x
    ADD r19 r18 r16

    SET r1 LOOP_X_RET
    JMP GET_PIXEL ; Call get pixel

    LOOP_X_RET:
        TEST r2
        ; Skip pixel if 0
        JE LOOP_X_CONTINUE
    
    ; x - 1, y
    SET r1 RET_1
    SUB r6 r16 1
    JMP GET_PIXEL

    RET_1:
        TEST r2
        JE ERODE

    ; x + 1, y
    SET r1 RET_2
    ADD r6 r16 1
    JMP GET_PIXEL

    RET_2:
        TEST r2
        JE ERODE

    ; Set X back
    OR r6 r16 r16

    ; x, y - 1
    SET r1 RET_3
    SUB r5 r15 1
    JMP GET_PIXEL

    RET_3:
        TEST r2
        JE ERODE
    
    ; x, y + 1
    SET r1 RET_4
    ADD r5 r15 1
    JMP GET_PIXEL

    RET_4:
        TEST r2
        JE ERODE

    ; Set output to 1
    STORE r31 r19    
    JMP LOOP_X_CONTINUE

    ; Erode and continue
    ERODE:
        ; Set output to 0
        STORE r30 r19

    LOOP_X_CONTINUE:
        ADD r16 r16 1
        CMP r16 20
        JL LOOP_X

LOOP_Y_CONTINUE:
    ADD r15 r15 1
    CMP r15 20
    JL LOOP_Y

LOOP_Y_END:
    HALT

; Called by setting
; r1 - ret address
; r2 - ret value
; r6, r5 (x, y)
GET_PIXEL:
    ; x < 0, return 0
	CMP r6 0
	JL GET_PIXEL_FAIL
	; x >= 20, return 0
	CMP r6 20
	JGE GET_PIXEL_FAIL
	; y < 0, return 0
	CMP r5 0 
	JL GET_PIXEL_FAIL
	; y >= 20, return 0
	CMP r5 20
	JGE GET_PIXEL_FAIL

    ; Base y * 20 + x
    MUL r20 r5 20
    ADD r20 r20 r6
    LOAD r2 r20
    JMP r1 ; ret

    GET_PIXEL_FAIL:
		SET r2 0 ; Set ret as 0
		JMP r1   ; Jump to ret address
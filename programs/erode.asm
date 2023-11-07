; Erode program
;
; 20 x 20 image
;	input  = 0x0000 (0 - 399)
;	output = 0x0190 (400 - 799)
;


; SP in R0
; RT in R1
; 
; RET in R2
; ARG1 in R3
; ARG2 in R4
; ARG3 in R5
; ARG4 in R6
; R7-R14 scratch


SYS_INIT:
	SET R0 800

MAIN:
	; init vars
	; input base in r30
	; output base in r31

	SET R30 0
	SET R31 400

	; for y in range 20
	; y in R15
	XOR R15 R15 R15
	LOOP_Y:
		; write base
		; base = 400 + y * 20
		MUL R17 R15 20
		ADD R17 R17 400

		; for x in range 20
		; x in R16
		XOR R16 R16 R16
		LOOP_X:
			;; if pixel = 0, continue
			; x, y
				SET R1 RET_0
				OR R3 R16 R16
				OR R4 R15 R15
				JMP GET_PIXEL
			RET_0:
				TEST R2
				JE LOOP_X_END


			;; if any neighbour = 0, erode 
			; x - 1, y
				SET R1 RET_1
				SUB R3 R16 1
				OR R4 R15 R15
				JMP GET_PIXEL
			RET_1:
				TEST R2
				JE ERODE

			; x + 1, y
				SET R1 RET_2
				ADD R3 R16 1
				OR R4 R15 R15
				JMP GET_PIXEL
			RET_2:
				TEST R2
				JE ERODE
			
			; x, y - 1
				SET R1 RET_3
				OR R3 R16 R16
				SUB R4 R15 1
				JMP GET_PIXEL
			RET_3:
				TEST R2
				JE ERODE
			
			; x, y + 1
				SET R1 RET_4
				OR R3 R16 R16
				ADD R4 R15 1
				JMP GET_PIXEL
			RET_4:
				TEST R2
				JE ERODE

			; no 0 neighbors, do nothing
			JMP LOOP_X_END

			ERODE:
				; [base + x] = 0
				ADD R2 R17 R16
				XOR R1 R1 R1
				STORE R1 R16

			LOOP_X_END:
				ADD R16 R16 1
				CMP R16 20
				JGE LOOP_X_EXIT
				JMP LOOP_X
		LOOP_X_EXIT:

		LOOP_Y_END:
			ADD R15 R15 1
			CMP R15 20
			JGE LOOP_Y_EXIT
			JMP LOOP_Y
	LOOP_Y_EXIT:

; END of program
HALT 

; getPixel(x, y)
GET_PIXEL:
	; x < 0, return 0
	CMP R3 0
	JL GET_PIXEL_FAIL
	; x >= 20, return 0
	CMP R3 20
	JGE GET_PIXEL_FAIL
	; y < 0, return 0
	CMP R4 0 
	JL GET_PIXEL_FAIL
	; y >= 20, return 0
	CMP R4 20
	JGE GET_PIXEL_FAIL

	; R7 = y * 20 + x
	MUL R7 R4 20
	ADD R7 R7 R3

	; return pixel at R7
	LOAD R2 R7
	JMP R1

	GET_PIXEL_FAIL:
		XOR R2 R2 R2
		JMP R1
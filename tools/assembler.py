def assemble_instruction(instruction):
    # Strip everything after ; (comments)
    instruction = instruction.split(";")[0].strip()

    tokens = instruction.split()
    tokens[0] = tokens[0].lower()

    if tokens[0] == "set":
        d = int(tokens[1])
        x = int(tokens[2])
        return (1 << 31) | (d << 26) | x

    elif tokens[0] == "alu":
        op_map = {
            "~": 0, "&": 1, "|": 2, "^": 3, "+": 4, "-": 5,
            "/": 6, "%": 7, "*": 8, ">>": 9, "<<": 10
        }
        op = op_map[tokens[1]]
        d = int(tokens[2])
        a = int(tokens[3])
        b = int(tokens[4])
        return (1 << 30) | (op << 24) | (d << 19) | (a << 14) | (b << 9)

    elif "j" in tokens[0]:  # Jump instructions
        jump_map = {
            "je": 1, "jg": 2, "jl": 4,
            "jge": 3, "jle": 5, "jne": 6, "jmp": 7
        }

        cond = jump_map[tokens[0]]
        x = int(tokens[1])
        return (1 << 29) | (cond << 26) | x

    elif tokens[0] == "load":
        d = int(tokens[1])
        x = int(tokens[2])
        return (1 << 28) | (d << 22) | x

    elif tokens[0] == "store":
        d = int(tokens[1])
        x = int(tokens[2])
        return (3 << 27) | (d << 22) | x

    elif tokens[0] == "test":
        d = int(tokens[1])

        return (1 << 27) | (d << 21)

    elif tokens[0] == "cmp":
        a = int(tokens[1])
        b = int(tokens[2])

        return (3 << 26) | (a << 21) | (b << 16)

    else:
        raise ValueError(f"Unknown instruction: {tokens[0]}")

def assemble_program(program):
    instructions = program.strip().split('\n')
    machine_code = [assemble_instruction(instr) for instr in instructions]
    return machine_code

# Example usage:
program = """
JMP 4     ; Jump to the second SET
SET 15 69 ; Set if cool
SET 0 0   ; Set if not cool (to not run forever)
JMP 5     ; Jump over the other reg 0 set
SET 0 10
SET 1 10
CMP 0 1
JE 1      ; Jump to the first SET
"""

machine_code = assemble_program(program)
for code in machine_code:
    print(f"{code}L,")

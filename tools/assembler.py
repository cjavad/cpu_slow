def assemble_instruction(instruction: str):
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

        x = 0
        reg = 0

        if tokens[0].startswith("r"):
            tokens[0] = tokens[0].removeprefix("r")
            reg = int(tokens[1])
        else:
            x = int(tokens[1])

        cond = jump_map[tokens[0]]



        return (1 << 29) | (cond << 26) | (bool(reg) << 25) | reg << 20 | x

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

    elif tokens[0] == "halt":
        return (1 << 0)

    elif tokens[0] == "nop":
        return (0 << 0)

    else:
        raise ValueError(f"Unknown instruction: {tokens[0]}")

def assemble_program(program):
    instructions = program.strip().split('\n')
    machine_code = [assemble_instruction(instr) for instr in instructions]
    return machine_code

# Example usage:
program = """
SET 0 16 ; Set constants
SET 1 57005
ALU << 0 1 0
SET 1 48879
ALU | 0 0 1
HALT
"""

machine_code = assemble_program(program)
for code in machine_code:
    print(f"{code}L,")

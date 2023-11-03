import sys
from typing import List


def parse_literal(l: str, bit_size=26, signed=False) -> int:
    if '0x' in l:
        integer = int(l.replace('0x', ''), 16)
    elif '0b' in l:
        integer = int(l.replace('0b', ''), 2)
    elif '0o' in l:
        integer = int(l.replace('0o', ''), 8)
    else:
        integer = int(l)

    if signed:
        max_size = (1 << (bit_size - 1)) - 1  # Maximum allowed size for the given bit length
        min_size = -(1 << (bit_size - 1))
    else:
        max_size = (1 << bit_size) - 1  # Maximum allowed size for the given bit length
        min_size = 0

    # If the number is negative, convert to unsigned binary representation
    if integer < 0 and not signed:
        integer = (1 << bit_size) + integer

    if signed:
        # Sign extend to bit_size
        integer = (integer + (1 << bit_size)) % (1 << bit_size)

    if integer < min_size or integer > max_size:
        raise ValueError(f"Integer {integer} is out of range for {bit_size} bits!")

    return integer


def reg_to_bin(reg_str):
    return int(reg_str[1:])

def is_register(val):
    """Check if the value represents a register."""
    return val.startswith('r') or val.startswith('R')


def alu_op_to_bin(op_str: str):
    op_str = op_str.lower().removesuffix('u')

    ops = {
        'not': 0,
        'and': 1,
        'or': 2,
        'xor': 3,
        'add': 4,
        'sub': 5,
        'div': 6,
        'mod': 7,
        'mul': 8,
        'shr': 9,
        'shl': 10
    }
    return ops[op_str]


def assemble_program(program) -> List[int]:
    # Remove all comments and empty lines
    # Support #, ; and //
    program = [
        line.split('#')[0].split(';')[0].split('//')[0].strip() for line in program
    ]

    program = [line for line in program if line]  # Remove empty lines

    print(program)

    # First pass: Collect labels
    label_address_map = {}
    memory_map = {}
    instructions = []

    instruction_address = 0

    for line in program:
        if line.endswith(':'):
            label = line[:-1]
            label_address_map[label] = instruction_address
        elif line.startswith('.'):
            tokens = line.split()
            memory_map[tokens[0][1:]] = parse_literal(tokens[1], 32)
        else:
            instructions.append(line)
            instruction_address += 1

    # Second pass: Generate machine code
    machine_code = []

    for instr in instructions:
        machine_code.append(assemble_instruction(instr, label_address_map))

    return machine_code, memory_map


def assemble_instruction(instr: str, label_address_map) -> int:
    tokens = instr.split()
    op = tokens[0].lower()

    if op == "set":
        return 1 << 31 | reg_to_bin(tokens[1]) << 26 | parse_literal(tokens[2], 26)

    elif op.removesuffix('u') in ["not", "add", "sub", "and", "or", "xor", "mul", "div", "mod", "shl", "shr"]:
        signed_bit = 'u' in op
        last_is_reg = is_register(tokens[3])

        # Add immediate
        if last_is_reg:
            source2 = reg_to_bin(tokens[3]) << 9
        else:
            source2 = parse_literal(tokens[3], 14, signed_bit)

        return 1 << 30 | alu_op_to_bin(op) << 26 | signed_bit << 25 | (not last_is_reg) << 24 | reg_to_bin(
            tokens[1]) << 19 | reg_to_bin(
            tokens[2]) << 14 | source2

    elif op in ["jmp", "je", "jne", "jg", "jl", "jge", "jle"]:
        jump_ops = {
            "jmp": 7,
            "je": 1,
            "jne": 6,
            "jg": 2,
            "jl": 4,
            "jge": 3,
            "jle": 5
        }

        op_code = jump_ops[op] << 26
        flag_bit = 1 if is_register(tokens[1]) and not tokens[1] in label_address_map else 0

        if flag_bit:  # If it's a register
            dest_reg = reg_to_bin(tokens[1]) << 20
            address = 0
        else:
            dest_reg = 0
            if tokens[1] in label_address_map:  # If it's a label
                address = label_address_map[tokens[1]]
            else:  # If it's a constant
                address = parse_literal(tokens[1], 16)

        return 1 << 29 | op_code | flag_bit << 25 | dest_reg | address

    elif op in ["load", "store"]:
        op_code = 1 << 28 if op == 'load' else 3 << 27
        dest_reg = reg_to_bin(tokens[1]) << 21
        flag_bit = 1 if is_register(tokens[2]) else 0
        addr_reg_or_const = reg_to_bin(tokens[2]) << 16 if flag_bit else parse_literal(tokens[2], 16)
        return op_code | flag_bit << 26 | dest_reg | addr_reg_or_const

    elif op in ["test", "testu"]:
        signed_bit = 'u' in op
        return 1 << 27 | signed_bit << 25 | reg_to_bin(tokens[1]) << 19

    elif op in ["cmp", "cmpu"]:
        signed_bit = 'u' in op
        last_is_reg = is_register(tokens[2])

        if last_is_reg:
            source2 = reg_to_bin(tokens[2]) << 14
        else:
            # Compare immediate
            source2 = parse_literal(tokens[2], 19, signed_bit)

        return 3 << 26 | signed_bit << 25 | (not last_is_reg) << 24 | reg_to_bin(tokens[1]) << 19 | source2

    elif op == "halt":
        return 1

    elif op == "nop":
        return 0

    else:
        raise ValueError(f"Unknown instruction: {instr}")



if __name__ == '__main__':
    # Read file from first argument
    args = sys.argv[1:]

    if len(args) == 0:
        print("Please specify a file to assemble!")
        exit(1)

    with open(args[0], 'r') as f:
        program = f.readlines()


    machine_code, memory_map = assemble_program(program)

    # Output as 32-bit packed binary values to a binary file called "out.bin"

    with open('out.bin', 'wb') as f:
        for instr in machine_code:
            f.write(instr.to_bytes(4, 'big'))

    # Write memory map to a file called "mem.bin"
    # Truncate the address, just add sequentially
    with open('mem.bin', 'wb') as f:
        for val in memory_map.values():
            f.write(val.to_bytes(4, 'big'))
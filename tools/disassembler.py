def disassemble_instruction(instr: int) -> str:
    # Use bit masks and shifts to extract information from instruction

    # Check if it's an ALU operation (second highest bit set)
    if instr & (1 << 30):
        op_code = (instr >> 26) & 0xF
        signed_bit = (instr >> 25) & 1
        immediate = (instr >> 24) & 1
        dest_reg = (instr >> 19) & 0x1F
        src1_reg = (instr >> 14) & 0x1F
        src2_val = instr & ((1 << 14) - 1)

        ops = ["not", "and", "or", "xor", "add", "sub", "div", "mod", "mul", "shr", "shl"]

        op_str = ops[op_code] + ("u" if signed_bit else "")
        if immediate:
            return f"{op_str} r{dest_reg} r{src1_reg} {src2_val}"
        else:
            return f"{op_str} r{dest_reg} r{src1_reg} r{src2_val & 0x1F}"

    # Check if it's a jump instruction (third highest bit set)
    elif instr & (1 << 29):
        jump_ops = ["jmp", "je", "jne", "jg", "jl", "jge", "jle"]
        op_code = (instr >> 26) & 0x7
        flag_bit = (instr >> 25) & 1
        dest_reg_or_const = instr & ((1 << 26) - 1)

        if flag_bit:
            return f"{jump_ops[op_code]} r{dest_reg_or_const >> 20}"
        else:
            return f"{jump_ops[op_code]} {dest_reg_or_const}"

    # Check if it's a load/store instruction
    elif instr & (1 << 28):
        is_load = (instr >> 28) & 1
        flag_bit = (instr >> 26) & 1
        dest_reg = (instr >> 21) & 0x1F
        addr_reg_or_const = instr & ((1 << 21) - 1)

        if flag_bit:
            return f"{'load' if is_load else 'store'} r{dest_reg} r{addr_reg_or_const >> 16}"
        else:
            return f"{'load' if is_load else 'store'} r{dest_reg} {addr_reg_or_const}"

    elif instr & (1 << 31):
        reg = (instr >> 26) & 0x1F
        val = instr & ((1 << 26) - 1)
        return f"set r{reg} {val}"

    elif (instr >> 26) == 3:
        signed_bit = (instr >> 25) & 1
        immediate = (instr >> 24) & 1
        dest_reg = (instr >> 19) & 0x1F
        src2_val = instr & ((1 << 19) - 1)

        op_str = "cmpu" if signed_bit else "cmp"
        if immediate:
            return f"{op_str} r{dest_reg} {src2_val}"
        else:
            return f"{op_str} r{dest_reg} r{src2_val >> 14}"

    # Check if it's a test or testu instruction (1 shifted left 27 times)
    elif (instr >> 27) == 1:
        signed_bit = (instr >> 25) & 1
        dest_reg = (instr >> 19) & 0x1F

        return f"{'testu' if signed_bit else 'test'} r{dest_reg}"

    # Check if it's a halt instruction
    elif instr == 1:
        return "halt"

    elif instr == 0:
        return "nop"
    # Check for other instructions...

    # For simplicity, not handling all cases (e.g., cmp/test)
    # Add more elif blocks as needed, following the above pattern

    else:
        return f"UNKNOWN INSTRUCTION: {instr}"


def disassemble_program(binary_data: bytes):
    # Convert the binary data into a list of integers
    instr_list = [int.from_bytes(binary_data[i:i+4], 'big') for i in range(0, len(binary_data), 4)]

    # Disassemble each instruction
    return [(instr, disassemble_instruction(instr)) for instr in instr_list]


if __name__ == '__main__':
    with open('out.bin', 'rb') as f:
        binary_data = f.read()

    disassembled_program = disassemble_program(binary_data)

    for i, (instr, line) in enumerate(disassembled_program):
        print(f"{hex(i).rjust(5)} : {hex(instr).rjust(10)} : {line}")
import subprocess

image = [
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, 255,   0,   0,   0,   0,
    0,   0,   0,   0, 255,   0,   0,   0,   0,   0,   0,   0,   0,   0, 255, 255, 255,   0,   0,   0,
    0,   0,   0, 255, 255, 255,   0,   0,   0,   0,   0,   0,   0, 255, 255, 255, 255, 255,   0,   0,
    0,   0, 255, 255, 255, 255, 255,   0,   0,   0,   0,   0,   0,   0, 255, 255, 255,   0,   0,   0,
    0,   0, 255, 255, 255, 255, 255, 255,   0,   0,   0,   0,   0,   0,   0, 255,   0,   0,   0,   0,
    0,   0, 255, 255, 255, 255, 255, 255,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0, 255, 255, 255, 255, 255, 255,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0, 255, 255, 255, 255,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0, 255, 255,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0, 255,   0,   0,   0,   0,   0,   0, 255, 255, 255,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, 255, 255, 255,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, 255, 255, 255,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, 255, 255, 255,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,
    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0
]

image.extend([0] * (2**16 - len(image)))

# Write to mem.hex as hex
with open('mem.hex', 'wb') as f:
    for instr in image:
        f.write(f"{instr:08x}\n".encode('utf-8'))

# Build the verilog iverilog -o sim ../generated/CPUTop.v simulate.v
subprocess.run(['iverilog', '-o', 'sim', '../generated/CPUTop.v', 'simulate.v'])

# Run the simulation
subprocess.run(['./sim'])

# Read the output (out.hex) strip // comments
with open('out.hex', 'r') as f:
    out = f.readlines()

def print_image(image):
    for i, p in enumerate(image):
         if i % 20 == 0:
             print("")
         print(str(p).ljust(5), end=" ")

memory = [int(line.split('//')[0], 16) for line in out if line.strip() != '' and not line.startswith('//')]
input_picture = memory[0:400]
output_picture = memory[400:800]

print("Input:")
print_image(input_picture)
print("\n\nOutput:")
print_image(output_picture)
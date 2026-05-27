import sys

def file_split(fname):
	file = open(fname)
	file_chars = file.read()
	file_lines = file_chars.splitlines()
	
	if len(file_lines) <= 0:
		print("File has no lines.")
		file.close()
		return

	start_line = file_lines[0].upper()
	split_line = start_line.split(',')

	if "NAME" not in split_line:
		print("Failed, name attribute not found.")
		file.close()
		return

	name_index = split_line.index("NAME")
	full_col_count = len(split_line) #The full number of columns in the file.
	
	col_count = 0
	for head in split_line:
		if len(head) > 0:
			col_count += 1

	print(f"{col_count} out of {full_col_count}")
	print(f"Name ends at {name_index + 1}")

	new_list = [file_lines[0]]

	for line in file_lines:
		if line.__eq__(file_lines[0]):
			continue

		split = line.split(',')
		column = col_count

		for n in range(full_col_count-1, -1, -1):
			if len(split[n]) > 0:
				column -= 1

			if column == name_index + 1:
				column = n
				break

		#Name ends at column.
		new_line = ""
		n = 0
		while n < len(split):
			if len(split[n]) <= 0:
				n += 1
				continue

			new_line += split[n]
			if n >= name_index and n < column-1:
				new_line += ' '
			else:
				new_line += ','

			n += 1

		new_line = new_line[:-1]
		new_list.append(new_line)
	
	print(new_list)

	file = open(fname, 'w')

	for i in range(len(new_list)):
		file.write(new_list[i])
		if i < len(new_list) - 1:
			file.write('\n')

	file.close()

def main(args):
	for arg in args:
		file_split(arg)

if __name__ == "__main__":
	main(sys.argv[1:])
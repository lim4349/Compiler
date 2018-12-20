func main() {
	var x int
	var z int = 0
	x = 410
	for x > 0 {
		x = x / 2
		z = z + x
	}
	write(z)
}

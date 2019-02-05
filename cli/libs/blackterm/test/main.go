package main

import (
	"fmt"

	"github.com/capitancambio/blackterm"
)

func main() {
	input := `#Markdown header
## Level 2 header
Normal text

This is a list:

* The first item
* The second item

Just saying, *this is quite important* so you pay attention.
But this is even **more important!**
And I'm sorry to break this to you but .... ***THIS IS THE MOST IMPORTANT THING EVER WRITTEN!!***

On the other hand you shouldn't ~~pay attention to this~~

[I'm a link](http://thisshouldbedisplayed.com)
![This is an image](http://actualurltotheimage.com/img.png)

This is how the source code span is generated
` + "```" + `
func (tr TerminalRenderer) CodeSpan(out *bytes.Buffer, text []byte) {
style := chalk.Green.NewStyle().WithBackground(chalk.Black).WithTextStyle(chalk.Bold)
out.WriteString(style.Prefix())
out.WriteString("\n[[[\n")
out.Write(text)
out.WriteString("\n"]]]\n")
out.WriteString(style.Suffix())
}
` + "```" + `

Inline code span ` + "`2+2`" + ` looks like this




`

	//input := "HEY!"
	//renderer := blackterm.NewTerminalRenderer()
	//out := blackfriday.Markdown([]byte(input), renderer, blackfriday.EXTENSION_STRIKETHROUGH)
	out := blackterm.Markdown([]byte(input))
	fmt.Printf("%s", out)

}

package blackterm

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/capitancambio/chalk"
	"github.com/russross/blackfriday"
)

/*
Process a subset of markdown elements and prints them with flying colours!
*/
func Markdown(input []byte) []byte {
	tr := NewTerminalRenderer()
	ret := blackfriday.Markdown(input, tr, blackfriday.EXTENSION_STRIKETHROUGH + blackfriday.EXTENSION_FENCED_CODE)
	//get rid of \n at the end of the text
	for len(ret) > 0 && ret[len(ret)-1] == '\n' {
		ret = ret[:len(ret)-1]
	}
	return ret
}

func MarkdownString(input string) string {
	return string(Markdown([]byte(input)))
}

type TerminalRenderer struct {
	Headers   map[int]chalk.Style
	DefHeader chalk.Style
	Bullet    string
}

func NewTerminalRenderer() TerminalRenderer {
	return TerminalRenderer{
		Headers: map[int]chalk.Style{
			1: chalk.Underline.NewStyle().WithForeground(chalk.Magenta),
		},
		DefHeader: chalk.Bold.NewStyle().WithForeground(chalk.Magenta),
		Bullet:    "  • ",
	}
}

func (tr TerminalRenderer) BlockCode(out *bytes.Buffer, text []byte, lang string) {
	out.WriteString("    " + chalk.Red.Color(chalk.Bold.TextStyle(indent(string(text), "    "))) + "\n")
}

func (tr TerminalRenderer) BlockQuote(out *bytes.Buffer, text []byte) {}
func (tr TerminalRenderer) BlockHtml(out *bytes.Buffer, text []byte)  {}
func (tr TerminalRenderer) Header(out *bytes.Buffer, text func() bool, level int, id string) {
	style, ok := tr.Headers[level]
	if !ok {
		style = tr.DefHeader
	}

	out.WriteString(style.Prefix())
	if text() {
		out.WriteByte('\n')
		out.WriteByte('\n')
	}

	out.WriteString(style.Suffix())

}
func (tr TerminalRenderer) HRule(out *bytes.Buffer) {}
func (tr TerminalRenderer) List(out *bytes.Buffer, text func() bool, flags int) {
	text()
	out.WriteString("\n")
}
func (tr TerminalRenderer) ListItem(out *bytes.Buffer, text []byte, flags int) {
	out.WriteString(tr.Bullet)
	out.WriteString(indent(string(text), "    "))
	out.WriteByte('\n')
}
func (tr TerminalRenderer) Paragraph(out *bytes.Buffer, text func() bool) {
	if text() {
		out.WriteString("\n\n")
	}
}
func (tr TerminalRenderer) Table(out *bytes.Buffer, header []byte, body []byte, columnData []int) {}
func (tr TerminalRenderer) TableRow(out *bytes.Buffer, text []byte)                               {}
func (tr TerminalRenderer) TableHeaderCell(out *bytes.Buffer, text []byte, flags int)             {}
func (tr TerminalRenderer) TableCell(out *bytes.Buffer, text []byte, flags int)                   {}
func (tr TerminalRenderer) Footnotes(out *bytes.Buffer, text func() bool)                         {}
func (tr TerminalRenderer) FootnoteItem(out *bytes.Buffer, name, text []byte, flags int)          {}
func (tr TerminalRenderer) TitleBlock(out *bytes.Buffer, text []byte) {

}

// Span-level callbacks{}
func (tr TerminalRenderer) AutoLink(out *bytes.Buffer, link []byte, kind int) {}
func (tr TerminalRenderer) CodeSpan(out *bytes.Buffer, text []byte) {
	out.WriteString(chalk.Red.Color(chalk.Bold.TextStyle(string(text))))
}
func (tr TerminalRenderer) DoubleEmphasis(out *bytes.Buffer, text []byte) {
	out.WriteString(chalk.Bold.TextStyle(string(text)))
}
func (tr TerminalRenderer) Emphasis(out *bytes.Buffer, text []byte) {
	out.WriteString(chalk.Italic.TextStyle(string(text)))
}
func (tr TerminalRenderer) Image(out *bytes.Buffer, link []byte, title []byte, alt []byte) {
	out.WriteString(fmt.Sprintf("[IMAGE: %s (%s)]", string(alt), string(link)))
}

func (tr TerminalRenderer) LineBreak(out *bytes.Buffer) {
	out.WriteString("\n")
}
func (tr TerminalRenderer) Link(out *bytes.Buffer, link []byte, title []byte, content []byte) {
	out.WriteString(fmt.Sprintf("%s (%s)", string(content), chalk.Underline.TextStyle(string(link))))
}
func (tr TerminalRenderer) RawHtmlTag(out *bytes.Buffer, tag []byte) {}
func (tr TerminalRenderer) TripleEmphasis(out *bytes.Buffer, text []byte) {
	out.WriteString(chalk.Italic.TextStyle(chalk.Bold.TextStyle(string(text))))
}
func (tr TerminalRenderer) StrikeThrough(out *bytes.Buffer, text []byte) {
	out.WriteString(chalk.Strikethrough.TextStyle(string(text)))
}
func (tr TerminalRenderer) FootnoteRef(out *bytes.Buffer, ref []byte, id int) {}

// Low-level callbacks{}
func (tr TerminalRenderer) Entity(out *bytes.Buffer, entity []byte) {
	out.Write(entity)
}

func (tr TerminalRenderer) NormalText(out *bytes.Buffer, text []byte) {
	out.Write(text)
}

// Header and footer{}
func (tr TerminalRenderer) DocumentHeader(out *bytes.Buffer) {}
func (tr TerminalRenderer) DocumentFooter(out *bytes.Buffer) {}
func (tr TerminalRenderer) GetFlags() int                    { return 0 }

func indent(s string, indent string) string {
	parts := strings.Split(s, "\n")
	result := parts[0]
	for _, part := range parts[1:] {
		result += "\n"
		result += indent
		result += part
	}
	return result
}

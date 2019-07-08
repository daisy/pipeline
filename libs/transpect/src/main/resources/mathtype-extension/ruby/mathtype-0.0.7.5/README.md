# Mathtype

This gem can read proprietary MathType binary equations that are usually embedded in Word documents and convert these equations into an XML form. This XML form can then be used for further processing, e.g. to convert the equation to MathML.

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'mathtype'
```

And then execute:

    $ bundle

Or install it yourself as:

    $ gem install mathtype

## Usage

To convert a MathType binary equation (e.g. extracted from a Word document) to an XML form:

```
xml = Mathtype::Converter.new("equation1.bin") # to get the Nokogiri XML object
puts xml.to_xml # to get the string XML representation
```

# Testing

Run `bundle exec rspec` to run specs.

## Contributing

1. Fork it ( https://github.com/[my-github-username]/mathtype/fork )
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request

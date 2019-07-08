# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'mathtype/version'

Gem::Specification.new do |spec|
  spec.name          = "mathtype"
  spec.version       = Mathtype::VERSION
  spec.authors       = ["Jure Triglav"]
  spec.email         = ["juretriglav@gmail.com"]
  spec.summary       = %q{ A Ruby gem for reading MathType binaries and converting them to an XML form. }
  spec.description   = %q{ This gem can read proprietary MathType binary equations that are usually embedded in Word documents and convert these equations into an XML form. This XML form can then be used for further processing, e.g. to convert the equation to MathML.}
  spec.homepage      = ""
  spec.license       = "MIT"

  spec.files         = `git ls-files -z`.split("\x0")
  spec.executables   = spec.files.grep(%r{^bin/}) { |f| File.basename(f) }
  spec.test_files    = spec.files.grep(%r{^(test|spec|features)/})
  spec.require_paths = ["lib"]

  spec.add_development_dependency "bundler", "~> 1.7"
  spec.add_development_dependency "rake", "~> 10.0"
  spec.add_development_dependency "rspec", "~> 3.3"
  spec.add_development_dependency "pry", "~> 0.10"
  spec.add_dependency "bindata", "~> 2.1"
  spec.add_dependency "nokogiri", "~> 1.6"
  spec.add_dependency "ruby-ole", "~> 1.2"
end

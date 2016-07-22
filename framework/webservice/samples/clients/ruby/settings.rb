class Settings
  TIMEOUT_SECONDS = 100
  BASEURI = "http://localhost:8181/ws"
  
  attr_accessor :options
  attr_accessor :command

  def initialize
    @options = {}
    @command = ""
  end

  @@instance = Settings.new

  def self.instance
    return @@instance
  end

  private_class_method :new

end

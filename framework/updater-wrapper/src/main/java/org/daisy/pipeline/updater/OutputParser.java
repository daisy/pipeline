package org.daisy.pipeline.updater;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class OutputParser {
        private Scanner scanner;
        private UpdaterObserver obs;
        private static final String INFO="INFO";
        private static final String ERROR="ERROR";
       
        /**
         * @param is
         * @param obs
         */
        public OutputParser(InputStream is, UpdaterObserver obs) {
                this.scanner = new Scanner(new BufferedInputStream(is));
                this.obs = obs;
        }

        public void parse(){
                LogLine ll;
                while (scanner.hasNextLine()){
                        ll=parseLine(scanner.nextLine());
                        if (ll.level==INFO){
                                obs.info(ll.message);
                        }else{
                                obs.error(ll.message);
                        }
                }
        }

        LogLine parseLine(String line){
                LogLine ll=new LogLine();
                ll.fromLine(line,INFO);
                if (!ll.gibberish){
                        return ll;
                }

                ll.fromLine(line,ERROR);
                if (!ll.gibberish){
                        return ll;
                }

                return ll;
        }


        class LogLine{
                private static final String FORMAT="[%s]";
                String level;
                String message;
                boolean gibberish;

                LogLine(){
                        this.gibberish=true;
                }

                void fromLine(String line,String level){
                        String brackets=String.format(FORMAT,level);
                        if (line.startsWith(brackets)){
                                this.message=line.substring(brackets.length()).trim();
                                this.level=level;
                                this.gibberish=false;
                        }
                }
                
        }
}

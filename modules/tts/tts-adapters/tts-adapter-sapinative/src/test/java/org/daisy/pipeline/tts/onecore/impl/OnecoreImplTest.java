package org.daisy.pipeline.tts.onecore.impl;

import com.xmlcalabash.util.TreeWriter;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.sapi.impl.SAPIEngine;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Testing the onecore engine initialisation and methods
 */
public class OnecoreImplTest {

    private static OnecoreService service;
    private static OnecoreEngine engine;



    @BeforeClass
    public static void load() throws TTSService.SynthesisException {
        service = new OnecoreService();

        Map<String,String> params = new HashMap();
        params.put("org.daisy.pipeline.tts.onecore.priority", "7");
        params.put("org.daisy.pipeline.tts.sapi.priority", "7");
        params.put("org.daisy.pipeline.tts.sapi.samplerate", "22050");
        params.put("org.daisy.pipeline.tts.sapi.bytespersample", "2");
        try{
            engine = (OnecoreEngine) service.newEngine(params);
        } catch (Throwable e){
            throw new TTSService.SynthesisException(e);
        }
    }

    @Test
    public void getAvailableVoices(){
        Collection<Voice> voices = engine.getAvailableVoices();
        for(Voice v: voices){
            System.out.println(v.toString());
        }
        Assert.assertTrue(voices.size() > 0);
    }

    public void allocateThreadResource() {
        try{
            TTSRegistry.TTSResource test = engine.allocateThreadResources();

        } catch (TTSService.SynthesisException e) {
            throw new RuntimeException(e);
        }


    }

    private static String SsmlNs = "http://www.w3.org/2001/10/synthesis";
    private static Processor Proc = new Processor(false);

    public XdmNode simpleTestSSML(String text) throws URISyntaxException {
        TreeWriter tw = new TreeWriter(Proc);
        tw.startDocument(new URI("http://test"));
        tw.startContent();
        tw.addStartElement(new QName(SsmlNs, "speak"));
        tw.addStartElement(new QName(SsmlNs, "s"));
        tw.addStartElement(new QName(SsmlNs, "y"));
        tw.addAttribute(new QName(null, "attr"), "attr-val");
        tw.addEndElement();
        tw.addText(text);
        tw.addEndElement();

        return tw.getResult();
    }

    @Test
    public void synthesizeOnecoreTest()
            throws URISyntaxException, SaxonApiException, SAXException, IOException, TTSService.SynthesisException, InterruptedException {

        Collection<Voice> voices = engine.getAvailableVoices();
        Voice selectedVoice = null;
        for (Voice v: voices ) {

            if(v.engine.equals("onecore")){
                selectedVoice = v;
                break;
            } else {
                System.out.println(v.engine);
            }
        }
        if(selectedVoice == null){
            throw new TTSService.SynthesisException("No onecore voice available for test");
        }

        TTSEngine.SynthesisResult result = engine.synthesize(
                simpleTestSSML("this a simple text"),
                selectedVoice,
                engine.allocateThreadResources()
        );

        Assert.assertTrue(result.audio.getFrameLength() > 5000);

    }
    @Test
    public void synthesizeSAPITest()
            throws URISyntaxException, SaxonApiException, SAXException, IOException, TTSService.SynthesisException, InterruptedException {

        Collection<Voice> voices = engine.getAvailableVoices();
        Voice selectedVoice = null;
        for (Voice v: voices ) {

            if(v.engine.equals("sapi")){
                selectedVoice = v;
                break;
            } else {
                System.out.println(v.engine);
            }
        }
        if(selectedVoice == null){
            throw new TTSService.SynthesisException("No sapi voice available for test");
        }

        TTSEngine.SynthesisResult result = engine.synthesize(
                simpleTestSSML("this a simple text"),
                selectedVoice,
                engine.allocateThreadResources()
        );
        Assert.assertTrue(result.audio.getFrameLength() > 5000);

    }

    @AfterClass
    public static void dispose() {
        service.deactivate();
    }
}

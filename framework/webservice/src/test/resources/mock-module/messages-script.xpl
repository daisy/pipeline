<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:mock-progress-messages"
                version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">_</h1>
        <p px:role="desc">_</p>
    </p:documentation>
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:declare-step type="px:sleep">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:option name="milliseconds" required="true"/>
    </p:declare-step>
    
    <p:choose px:message="a" px:progress=".5">
        <p:when test="1>0">
        
            <!-- 0 -->
            
            <px:sleep milliseconds="2000" px:message="b" px:progress=".5"/>
            <p:group px:message="c" px:progress=".5">
                
                <!-- .25 -->
                
                <px:sleep milliseconds="2000" px:message="d" px:progress=".5"/>
                
                <!-- .375 -->
                
                <px:sleep milliseconds="2000" px:message="e" px:progress=".5"/>
            </p:group>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
    <!-- .5 -->
    
    <px:sleep milliseconds="2000" px:progress=".05"/>
    
    <p:group px:progress=".25">
        
        <!-- .55 -->
        
        <px:sleep milliseconds="2000" px:message="f" px:progress=".5"/>
        
        <!-- .675 -->
        
        <px:sleep milliseconds="2000" px:message="g" px:progress=".5"/>
    </p:group>
    
    <!-- .8 -->
    
    <px:sleep milliseconds="2000" px:message="h" px:progress=".1"/>
    
    <!-- .9 -->
    
    <px:sleep milliseconds="2000"/>
    
</p:declare-step>

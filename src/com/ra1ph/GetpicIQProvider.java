package com.ra1ph;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 19.06.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public class GetpicIQProvider implements IQProvider {
    @Override
    public IQ parseIQ(final XmlPullParser parser) throws Exception {
        IQ iq = new IQ() {
            @Override
            public String getChildElementXML() {
                String value = null;


                return MainThread.GET_XML;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        return iq;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

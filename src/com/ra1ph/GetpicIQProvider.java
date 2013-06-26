package com.ra1ph;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;
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
            public String getXmlns() {
                return "";    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public String getChildElementXML() {
                String value = null;
                StringBuilder sb = new StringBuilder();
                int event = 0;
                try {
                    event = parser.getEventType();

                    // get the content
                    while (true) {
                        switch (event) {
                            case XmlPullParser.TEXT:
                                // We must re-escape the xml so that the DOM won't throw an exception
                                sb.append(StringUtils.escapeForXML(parser.getText()));
                                break;
                            case XmlPullParser.START_TAG:
                                sb.append('<').append(parser.getName()).append('>');
                                break;
                            case XmlPullParser.END_TAG:
                                sb.append("</").append(parser.getName()).append('>');
                                break;
                            default:
                        }

                        if (event == XmlPullParser.END_TAG && "vCard".equals(parser.getName())) break;

                        event = parser.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return value;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        return iq;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

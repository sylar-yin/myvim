package vjde.completion;
import java.util.ArrayList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.util.List;
interface Result {
        public String name() ;
        public String location() ;
        public String value() ;
    /*
        public String value;
        public String location;
        public String name;
        public String name() {
            return name;
        }
        public String location() {
            return location;
        }
        public String value() {
            return value;
        }
        */
    }
interface Action {
        public Result[] results() ;
        public String value() ;
    };
class ResultImpl2 implements Result {
	String name;
	String location;
	public ResultImpl2(String n,String l) {
		name = n;
		location = l;
	}
    public String value() {
        return "";
    }
				public String name() {
					return name==null || name.length()==0 ? "success": name;
				}
				public String location() {
					if ( location.indexOf('\n')>=0) {
						return "";
					}
					return location.trim();
				}
				public String[] params() {
					return new String[0];
				}
				public String type() {
					return null;
				}
				public Class annotationType() {
					return null;
				}
}
class ActionImpl2 implements Action {
	String name ;
	Result[] res;
	public ActionImpl2(String n,Result[] r) {
		name = n;
		res = r;
	}
				public String value() {
					return name;
				}
				public Result[] results() {
					return res;
				}
                /*
				public InterceptorRef[] interceptorRefs() {
					return null;
				}
				public ExceptionMapping[] exceptionMappings() {
					return null;
				}
                */
				public String[] params() {
					return new String[0];
				}
				public Class annotationType() {
					return null;
				}
}

public class Struts2Simple {
	XPathExpression exprpackage = null ;
	XPathExpression expriclude= null ;
	XPathExpression expkg= null ;
	static XPathFactory factory = XPathFactory.newInstance();
	static XPath xpath = factory.newXPath();
	DocumentBuilder db = null ;
    String destUrl=null;
	private static class MyAction {
		public MyAction(Action a, String sp,String m,String k) {
			action = a;
			space = sp;
			klass = k;
			method  = m==null?"execute":m;
			url = space +"/"+ action.value();
		}
		public String url;
		public Action action;
		public String space;
		public String method;
		public String klass;
	}
	DynamicClassLoader dcl = null ;
	String webapp;
	String classpath;
	String actionpkg;
	String classname;
	String currentPkg=null;
	ArrayList<MyAction> actions = new ArrayList<MyAction>();
	public Struts2Simple(String webapp,String path)
	{
		classpath = path;
		this.webapp=webapp;
		dcl = new DynamicClassLoader(classpath);
	}
	public void toOut() {
		for ( MyAction act : actions) {
			System.out.println(act.klass);
			System.out.println(act.space);
			System.out.println(act.method);
			System.out.println(act.action.value());
		}
	}
	public StringBuffer result2Dict(MyAction action) {
		StringBuffer b = new StringBuffer();
		b.append('{') ;
		char sp=' ';
		for (Result res : action.action.results()) {
			b.append(String.format(" %3$c '%1$s' : '%2$s'",res.name(),res.location().replaceAll("'","''"),sp));
			sp = ',';
		}
		b.append('}');
		return b;
	}
	public StringBuffer action2List(MyAction a) {
		StringBuffer b = new StringBuffer();
		b.append(String.format("['%1$s' , '%2$s' , '%3$s' , %4$s ]",a.klass, a.method,a.url,result2Dict(a).toString()));
		return b;
	}
	public String actions2vim() {
		StringBuffer b = new StringBuffer();
		b.append("[\n");
		for ( MyAction action : actions) {
			b.append("\\");
			b.append(action2List(action).toString());
			b.append(',');
			b.append("\n");
		}
		b.append("\\]");
		return b.toString();
	}
	public Document findXml(String xml) {
		//System.out.println(xml);
		if ( db == null ) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setValidating(false);
			try {
				db = dbf.newDocumentBuilder();
			}
			catch(ParserConfigurationException e1) {
				e1.printStackTrace(System.err);
				return null;
			}
		}
			long s = System.currentTimeMillis();
		try {
			return db.parse(new FileInputStream(xml));
		}
		catch(IOException e2) {
			e2.printStackTrace(System.err);
		}
		catch(SAXException e1) {
			e1.printStackTrace(System.err);
		}
		finally {
			//System.out.println("parser "  + (System.currentTimeMillis()-s));
		}
		return null;
	}
	public Document findStruts2(String webapp)
	{
			return findXml(webapp+"/WEB-INF/classes/struts.xml");
	}
	public Document findWeb(String webapp)
	{
			return findXml(webapp+"/WEB-INF/web.xml");
	}
	public NodeList findInclues(Document doc) {
		if ( expriclude== null ) {
			try {
				expriclude= xpath.compile("//struts/include");
			}
			catch(XPathExpressionException e1) {
			}
		}
		try {
			return (NodeList ) expriclude.evaluate(doc, XPathConstants.NODESET);
		}
		catch(XPathExpressionException e1) {
		}
		return null;
		//return doc.getElementsByTagName("include");
	}
	public NodeList findPackages(Document doc) {
		if ( expkg== null ) {
			try {
				expkg= xpath.compile("//struts/package");
			}
			catch(XPathExpressionException e1) {
			}
		}
		try {
			return (NodeList) expkg.evaluate(doc, XPathConstants.NODESET);
		}
		catch(XPathExpressionException e1) {
		}
		return null;
		//return doc.getElementsByTagName("package");
	}
	public boolean findInxml() {
		return handleXml( findStruts2(webapp));
	}
	public boolean handleXml(Document doc) {
		//long s = System.currentTimeMillis();
		if ( doc != null ) {
			NodeList incs = findInclues(doc);
			for ( int i = 0 ; i < incs.getLength(); i++ ) {
				Element e = (Element) incs.item(i);
				String file=e.getAttribute("file");
				Document dd2 = findXml(webapp+"/WEB-INF/classes/" + file);
				if ( dd2 != null ) {
					if (handleXml(dd2)) {
                        return true;
                    }
				}
				//System.out.println(file);
			}
			NodeList pkgs = findPackages(doc);
			for ( int i = 0 ; i < pkgs.getLength() ; i++) {
				Element e = (Element) pkgs.item(i);
				if (onPackage(e)){
                    return true;
                }
			}
		}
        return false;
		//System.out.println(System.currentTimeMillis()-s);
	}
	public Result[] onResult(Element act) {
		NodeList paction = act.getElementsByTagName("result");
		Result[] res = new Result[paction.getLength()];
		for ( int i = 0 ; i <  paction.getLength() ; i++ ) {
			Element e = (Element) paction.item(i);
			final String name = e.getAttribute("name");
			final String location = e.getTextContent();
			res[i] = new ResultImpl2(name,location);
			/*
			res[i] = new Result() {
				public String name() {
					return name==null || name.length()==0 ? "success": name;
				}
				public String location() {
					if ( location.indexOf('\n')>=0) {
						return "";
					}
					return location.trim();
				}
				public String[] params() {
					return new String[0];
				}
				public String type() {
					return null;
				}
				public Class annotationType() {
					return null;
				}
			};
			*/

			//System.out.println(location);
		}
		return res;
	}
    public boolean compareUrl(String source,String actionurl)
    {
        //System.out.println(source);
        //System.out.println("==>" + actionurl);
        if ( source.compareTo(actionurl)==0) {
            return true;
        }
        if ( actionurl.indexOf('*')<0) {
            return false;
        }
        String pattern[] = actionurl.split("\\*");
        int start=0;
        for ( int i = 0 ; i < pattern.length; i++ ) {
            //System.out.println(pattern[i]);
            start = source.indexOf(pattern[i],start);
            if ( start<0) {
                return false;
            }
        }
        return true;
    }
	public boolean onPackage(Element pkg) {
		Object result = null ;
		if ( exprpackage == null ) {
			try {
				exprpackage = xpath.compile("action");
			}
			catch(XPathExpressionException e1) {
			}
		}
		try {
			result = exprpackage.evaluate(pkg, XPathConstants.NODESET);
		}
		catch(XPathExpressionException e1) {
		}
		if ( result == null ) {
			return false;
		}

		NodeList paction = (NodeList) result;
		//NodeList paction = pkg.getElementsByTagName("action");
		String space = pkg.getAttribute("namespace");
		if ( space == null ) {
			space = "/";
		}
		for ( int i = 0 ; i <  paction.getLength() ; i++ ) {
			final Element e = (Element) paction.item(i);
			final String name = e.getAttribute("name");
			//System.out.println(name);
			final String klass = e.getAttribute("class");
			String me = e.getAttribute("method");
			if  ( me == null || me.length() ==0 ) {
				me = "execute";
			}
			Result[] res = onResult(e);
			Action action = new ActionImpl2(name,res);
			/*
			final Action action = new Action() {
				public String value() {
					return name;
				}
				public Result[] results() {
					return onResult(e);
				}
				public InterceptorRef[] interceptorRefs() {
					return null;
				}
				public ExceptionMapping[] exceptionMappings() {
					return null;
				}
				public String[] params() {
					return new String[0];
				}
				public Class annotationType() {
					return null;
				}
			};
			*/

            if (destUrl!=null) {
                String url = space+"/" + action.value();
                if ( compareUrl(destUrl,url)) {
                    actions.add( new MyAction(action,space,me,klass));
                    return true;
                }
            }
            else {
                actions.add( new MyAction(action,space,me,klass));
            }
		}
        return false;
	}
    public static void main(String args[]) {
		if ( args.length < 2) {
			System.err.println("<webapp-dir>  [url]");
			return;
		}

		Struts2Simple s2c = new Struts2Simple(args[0],args[1]);
        //System.out.println(s2c.compareUrl("/nowuser/LoginAction","/nowuser/LoginAction"));
        //System.out.println(s2c.compareUrl("/nowuser/UserAddAction","/nowuser/*AddAction"));
        //System.out.println(s2c.compareUrl("/nowuser/UserDelAction","/nowuser/*AddAction"));
        if ( args.length>=2) {
            s2c.destUrl=args[1];
        }
        //System.out.println(s2c.destUrl);
		s2c.findInxml();
		System.out.println(s2c.actions2vim());
    }
}

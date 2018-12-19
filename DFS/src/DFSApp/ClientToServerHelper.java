package DFSApp;


/**
* DFSApp/ClientToServerHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from DFS.idl
* Thursday, December 6, 2018 at 3:37:39 PM Eastern Standard Time
*/

abstract public class ClientToServerHelper
{
  private static String  _id = "IDL:DFSApp/ClientToServer:1.0";

  public static void insert (org.omg.CORBA.Any a, DFSApp.ClientToServer that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static DFSApp.ClientToServer extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (DFSApp.ClientToServerHelper.id (), "ClientToServer");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static DFSApp.ClientToServer read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ClientToServerStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, DFSApp.ClientToServer value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static DFSApp.ClientToServer narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DFSApp.ClientToServer)
      return (DFSApp.ClientToServer)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      DFSApp._ClientToServerStub stub = new DFSApp._ClientToServerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static DFSApp.ClientToServer unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DFSApp.ClientToServer)
      return (DFSApp.ClientToServer)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      DFSApp._ClientToServerStub stub = new DFSApp._ClientToServerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}

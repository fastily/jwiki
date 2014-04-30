package jwiki.core;

public class BC
{
	public static void main(String[] args) throws Throwable
	{
		//System.out.println(FLogin.getPXFor("Fastily"));
		
		boolean x = "{{{{Foo}}|{{Bar}}|{{Test}}|{{Boo}}}}".matches("(?si)\\{\\{(\\{\\{.*?\\}\\}|\\|)*?\\}\\}");
		System.out.println(x);
		
	}
}
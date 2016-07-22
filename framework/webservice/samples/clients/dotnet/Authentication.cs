using System;
using System.Text;
using System.Security.Cryptography;
using System.Web;

namespace PipelineWSClient
{
	public class Authentication
	{
		private static string AUTH_ID = "clientid";
		private static string SECRET = "supersecret";
		
		public static string PrepareAuthenticatedUri(string uri)
		{
			string uristring = null;
  			string timestamp = DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ");
  			string nonce = GenerateNonce();
  			string query = String.Format("authid={0}&time={1}&nonce={2}", AUTH_ID, timestamp, nonce);
  			if (uri.Contains("?"))
			{
				uristring = String.Format("{0}&{1}", uri, query);
			}
			else
			{
				uristring = String.Format("{0}?{1}", uri, query);
			}
		
			string hash = GenerateHash(uristring);
  			string generatedUri = String.Format("{0}&sign={1}", uristring, HttpUtility.UrlEncode(hash));
			return generatedUri;
		}
		
		public static string GenerateHash(string data)
		{
			byte[] dataBytes = Encoding.UTF8.GetBytes(data);
			byte[] secretBytes = Encoding.UTF8.GetBytes(SECRET);
			HMAC hmac = new HMACSHA1(secretBytes);
			byte[] hashBytes = hmac.ComputeHash(dataBytes);
			string signature = Convert.ToBase64String(hashBytes);
			return signature;
		}
	
		public static string GenerateNonce()
		{
			Random rand = new Random();
			int n = rand.Next(1978515, 5158791);
			long retval = (long)n + DateTime.Now.Ticks;
			
			return retval.ToString();
		}
	}
}


package org.daisy.pipeline.tts.acapela;

import java.nio.IntBuffer;

import com.ochafik.lang.jnaerator.runtime.CharByReference;
import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface NscubeLibrary extends Library {
	public static String JNA_LIBRARY_NAME = NsCubeLoader.GetLibName();

	public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary
	        .getInstance(NscubeLibrary.JNA_LIBRARY_NAME);
	public static final NscubeLibrary INSTANCE = (NscubeLibrary) Native.loadLibrary(
	        NscubeLibrary.JNA_LIBRARY_NAME, NscubeLibrary.class);

	/**
	 * <i>native declaration : nscube_forjna.h:9</i><br> enum values
	 */
	public static interface NSC_AFTYPE_ENUM {
		/** <i>native declaration : nscube_forjna.h:5</i> */
		public static final int NSC_AF_DEFAULT = 0;
		/** <i>native declaration : nscube_forjna.h:6</i> */
		public static final int NSC_AF_LOCAL = 1;
		/** <i>native declaration : nscube_forjna.h:7</i> */
		public static final int NSC_AF_INET = 2;
		/** <i>native declaration : nscube_forjna.h:8</i> */
		public static final int NSC_AF_DIRECT = 3;
	};

	/**
	 * <i>native declaration : nscube_forjna.h:16</i><br> enum values
	 */
	public static interface NSC_SRVTYPE_ENUM {
		/** <i>native declaration : nscube_forjna.h:11</i> */
		public static final int NSC_STYPE_STANDALONE = 0;
		/** <i>native declaration : nscube_forjna.h:12</i> */
		public static final int NSC_STYPE_MASTER = 1;
		/** <i>native declaration : nscube_forjna.h:13</i> */
		public static final int NSC_STYPE_SLAVE = 2;
		/** <i>native declaration : nscube_forjna.h:14</i> */
		public static final int NSC_STYPE_MASTER_SLAVE = 3;
		/** <i>native declaration : nscube_forjna.h:15</i> */
		public static final int NSC_STYPE_INPROCESS = 4;
	};

	/**
	 * <i>native declaration : nscube_forjna.h:22</i><br> enum values
	 */
	public static interface NSC_SRVSTATUS_ENUM {
		/** <i>native declaration : nscube_forjna.h:19</i> */
		public static final int NSC_SSTAT_OFF = 0;
		/** <i>native declaration : nscube_forjna.h:20</i> */
		public static final int NSC_SSTAT_RUNNING = 1;
		/** <i>native declaration : nscube_forjna.h:21</i> */
		public static final int NSC_SSTAT_DOWN = 2;
	};

	/**
	 * <i>native declaration : nscube_forjna.h:30</i><br> enum values
	 */
	public static interface NSC_AUTHCTRL_ENUM {
		/** <i>native declaration : nscube_forjna.h:25</i> */
		public static final int NSC_STDRTP = 0;
		/** <i>native declaration : nscube_forjna.h:26</i> */
		public static final int NSC_ULRTP = 1;
		/** <i>native declaration : nscube_forjna.h:27</i> */
		public static final int NSC_BADRTP = 2;
		/** <i>native declaration : nscube_forjna.h:28</i> */
		public static final int NSC_HNMRTP = 3;
		/** <i>native declaration : nscube_forjna.h:29</i> */
		public static final int NSC_TCMRTP = 4;
	};

	/**
	 * <i>native declaration : nscube_forjna.h:41</i><br> enum values
	 */
	public static interface NSC_PRMTYPE_ENUM {
		/** <i>native declaration : nscube_forjna.h:32</i> */
		public static final int NSC_PRM_PITCH = 1;
		/** <i>native declaration : nscube_forjna.h:33</i> */
		public static final int NSC_PRM_SPEED = 2;
		/** <i>native declaration : nscube_forjna.h:34</i> */
		public static final int NSC_PRM_VOL = 3;
		/** <i>native declaration : nscube_forjna.h:35</i> */
		public static final int NSC_PRM_EVTMASK = 4;
		/** <i>native declaration : nscube_forjna.h:36</i> */
		public static final int NSC_PRM_SHAPE = 5;
		/** <i>native declaration : nscube_forjna.h:37</i> */
		public static final int NSC_PRM_SELBREAK = 6;
		/** <i>native declaration : nscube_forjna.h:38</i> */
		public static final int NSC_PRM_PRESEL = 7;
		/** <i>native declaration : nscube_forjna.h:39</i> */
		public static final int NSC_PRM_TTS_TIMEOUT = 8;
		/** <i>native declaration : nscube_forjna.h:40</i> */
		public static final int NSC_PRM_LAST = 9;
	};

	/**
	 * <i>native declaration : nscube_forjna.h:55</i><br> enum values
	 */
	public static interface NSC_EVID_ENUM {
		/** <i>native declaration : nscube_forjna.h:44</i> */
		public static final int NSC_EVID_TEXT_STARTED = 0;
		/** <i>native declaration : nscube_forjna.h:45</i> */
		public static final int NSC_EVID_TEXT_DONE = 1;
		/** <i>native declaration : nscube_forjna.h:46</i> */
		public static final int NSC_EVID_WORD_SYNCH = 2;
		/** <i>native declaration : nscube_forjna.h:47</i> */
		public static final int NSC_EVID_PHO_SYNCH = 3;
		/** <i>native declaration : nscube_forjna.h:48</i> */
		public static final int NSC_EVID_BOOKMARK = 4;
		/** <i>native declaration : nscube_forjna.h:49</i> */
		public static final int NSC_EVID_TTSERROR = 5;
		/** <i>native declaration : nscube_forjna.h:50</i> */
		public static final int NSC_EVID_STOP_DONE = 6;
		/** <i>native declaration : nscube_forjna.h:51</i> */
		public static final int NSC_EVID_MOUTH_POS = 7;
		/** <i>native declaration : nscube_forjna.h:52</i> */
		public static final int NSC_EVID_PHO_SYNCH_EXT = 8;
		/** <i>native declaration : nscube_forjna.h:53</i> */
		public static final int NSC_EVID_BOOKMARK_EXT = 9;
		/** <i>native declaration : nscube_forjna.h:54</i> */
		public static final int NSC_EVID_LAST = 10;
	};

	/**
	 * <i>native declaration : nscube_forjna.h:156</i><br> enum values
	 */
	public static interface NSC_SRVPROT_ENUM {
		/** <i>native declaration : nscube_forjna.h:151</i> */
		public static final int NSC_SPROT_DONGLE = 1;
		/** <i>native declaration : nscube_forjna.h:152</i> */
		public static final int NSC_SPROT_OEM = 2;
		/** <i>native declaration : nscube_forjna.h:153</i> */
		public static final int NSC_SPROT_EVAL = 3;
		/** <i>native declaration : nscube_forjna.h:154</i> */
		public static final int NSC_SPROT_EXPIRED = 4;
		/** <i>native declaration : nscube_forjna.h:155</i> */
		public static final int NSC_SPROT_TIME = 5;
	};

	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_NOT_ENOUGH_MEMORY = (int) -1;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int GREEK = (int) 1032;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_STOPPED = (int) 9;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int ENGLISH = (int) 2057;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int TURKISH = (int) 1055;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_INVALID_PARAM = (int) -10;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_VOICE_LIST = (int) -5;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int KOREAN = (int) 1042;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int SPANISH = (int) 1034;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int FINLANDSWEDISH = (int) 2077;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int GERMAN = (int) 1031;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int SWEDISH = (int) 1053;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int FINNISH = (int) 1035;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int CZECH = (int) 1029;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_NO_MATCHING_VOICE = (int) 1;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int USSPANISH = (int) 21514;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_VOICE_ENCODING_MU_LAW = (int) 7;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int MAXCHAR_PHONEME = (int) 2;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int BRAZILIAN = (int) 1046;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_INVALID_FREQ = (int) -21;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_PHO_SYNCH_EXT = (int) 128;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_WORD_SYNCH = (int) 2;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int CHINESE = (int) 2052;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int MANDARINCHINESE = (int) 2052;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_RANGE_PARAM = (int) -11;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int DANISH = (int) 1030;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_CONNRESET = (int) -16;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_STOP_DONE = (int) 32;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_LOCKED = (int) 5;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int AUSTRALIAN = (int) 3081;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_TIMEOUT_EVENT = (int) 4;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_INVALID_CHANNEL = (int) -3;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_NOTSTARTED = (int) 3;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_TIMEOUT = (int) 14;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int FINNISHSWEDISH = (int) 2077;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int CANADIANFRENCH = (int) 3084;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int DUTCH = (int) 1043;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_TCMRTP = (int) 4;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ALREADY_STARTED = (int) 2;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_BOOKMARK = (int) 8;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int ICELANDIC = (int) 1039;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_NOTEXT = (int) 10;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int GOTHENBURGSWEDISH = (int) 58397;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_CHANNEL_NOTREADY = (int) -13;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int POLISH = (int) 1045;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_VOICE_ENCODING_A_LAW = (int) 6;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int JAPANESE = (int) 1041;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int ITALIAN = (int) 1040;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int AMERICAN = (int) 1033;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CLIENT_CONNECTED = (int) 8;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_SRV_NOTOEM_PROT = (int) 13;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_MAX_VOICE_LEN = (int) 255;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int ARABIC_SA = (int) 1025;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_MOUTH_POS = (int) 64;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int FRENCH = (int) 1036;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int BRITISH = (int) 2057;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_CONNREJECT = (int) -17;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_IDVOICE = (int) -6;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_PROCESS_DATA = (int) -8;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_CHANNEL_LOCKED = (int) -12;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_UNLOCKED = (int) 6;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int ARABIC = (int) 5121;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int FAROESE = (int) 1080;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_INVALID_SERVER = (int) -4;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_CONNECT = (int) -14;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_CHANNEL_PAUSED = (int) 11;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_CHANNEL_DOWN = (int) -23;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int BELGIANFRENCH = (int) 2060;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_ACTIVE_CHANNEL = (int) -22;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_TTSERROR = (int) 16;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_SRV_NOTRUNNING = (int) 12;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NORWEGIAN = (int) 1044;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_VOICE_INIT = (int) -7;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int INDIANENGLISH = (int) 16393;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int USENGLISH = (int) 1033;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_STDRTP = (int) 0;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_PHO_SYNCH = (int) 4;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_NOT_IMPLEMENTED_YET = (int) -50;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ULTRP = (int) 1;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int RUSSIAN = (int) 1049;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_EXEC_STARTED = (int) -9;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_VOICE_ENCODING_PCM = (int) 1;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_TOOMANY_VOICES = (int) -18;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_EVTBIT_TEXT = (int) 1;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_INVALID_FORMAT = (int) -20;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int SCANIAN = (int) 62493;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int MAX_BOOKMARK_SIZE = (int) 50;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int MAX_IPA_PAR_PHO = (int) 2;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int BELGIANDUTCH = (int) 2067;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_AUTO_DISPATCH = (int) 7;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_INVALID_HANDLE = (int) -2;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_AUDIO_INIT = (int) -19;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int PORTUGUESE = (int) 2070;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_ERR_TIMEOUT_INIT = (int) -15;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int CATALAN = (int) 1027;
	/** <i>native declaration : nscube_forjna.h</i> */
	public static final int NSC_OK = (int) 0;

	/**
	 * Prototype of the call back function used to retreive the sound data from
	 * the TTS<br> <i>native declaration : nscube_forjna.h:158</i>
	 */
	public interface PNSC_FNSPEECH_DATA extends Callback {
		int apply(Pointer pData, int cbDataSize, NSC_SOUND_DATA pSoundData,
		        Pointer pAppInstanceData);
	};

	/**
	 * Prototype of the call back function used to retreive the events from the
	 * TTS<br> <i>native declaration : nscube_forjna.h:160</i>
	 */
	public interface PNSC_FNSPEECH_EVENT extends Callback {
		int apply(int nEventID, int cbEventDataSize, NSC_EVENT_DATA pEventData,
		        Pointer pAppInstanceData);
	};

	/**
	 * Original signature : <code>nscRESULT nscCreateServerContext(int, int,
	 * const char*, nscHSRV*)</code><br> <i>native declaration :
	 * nscube_forjna.h:171</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscCreateServerContext(int, int, java.lang.String, com.sun.jna.ptr.PointerByReference)}
	 *             and
	 *             {@link #nscCreateServerContext(int, int, com.sun.jna.Pointer, com.sun.jna.ptr.PointerByReference)}
	 *             instead
	 */
	@Deprecated
	int nscCreateServerContext(int nAddressFamily, int nPort, Pointer pSrvAddress,
	        PointerByReference phSrv);

	/**
	 * Original signature : <code>nscRESULT nscCreateServerContext(int, int,
	 * const char*, nscHSRV*)</code><br> <i>native declaration :
	 * nscube_forjna.h:171</i>
	 */
	int nscCreateServerContext(int nAddressFamily, int nPort, String pSrvAddress,
	        PointerByReference phSrv);

	/**
	 * Original signature : <code>nscRESULT nscCreateServerContextEx(int, int,
	 * int, const char*, nscHSRV*)</code><br> <i>native declaration :
	 * nscube_forjna.h:173</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscCreateServerContextEx(int, int, int, java.lang.String, com.sun.jna.ptr.PointerByReference)}
	 *             and
	 *             {@link #nscCreateServerContextEx(int, int, int, com.sun.jna.Pointer, com.sun.jna.ptr.PointerByReference)}
	 *             instead
	 */
	@Deprecated
	int nscCreateServerContextEx(int nAddressFamily, int nCmdPort, int nDataPort,
	        Pointer pSrvAddress, PointerByReference phSrv);

	/**
	 * Original signature : <code>nscRESULT nscCreateServerContextEx(int, int,
	 * int, const char*, nscHSRV*)</code><br> <i>native declaration :
	 * nscube_forjna.h:173</i>
	 */
	int nscCreateServerContextEx(int nAddressFamily, int nCmdPort, int nDataPort,
	        String pSrvAddress, PointerByReference phSrv);

	/**
	 * Original signature : <code>nscRESULT
	 * nscReleaseServerContext(nscHSRV)</code><br> <i>native declaration :
	 * nscube_forjna.h:175</i>
	 */
	int nscReleaseServerContext(Pointer hSrv);

	/**
	 * Original signature : <code>nscRESULT nscGetServerType(nscHSRV,
	 * NSC_SRVTYPE_ENUM*)</code><br> <i>native declaration :
	 * nscube_forjna.h:177</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscGetServerType(com.sun.jna.Pointer, java.nio.IntBuffer)}
	 *             and
	 *             {@link #nscGetServerType(com.sun.jna.Pointer, com.sun.jna.ptr.IntByReference)}
	 *             instead
	 */
	@Deprecated
	int nscGetServerType(Pointer hSrv, IntByReference pServerType);

	/**
	 * Original signature : <code>nscRESULT nscGetServerType(nscHSRV,
	 * NSC_SRVTYPE_ENUM*)</code><br> <i>native declaration :
	 * nscube_forjna.h:177</i>
	 */
	int nscGetServerType(Pointer hSrv, IntBuffer pServerType);

	/**
	 * Original signature : <code>nscRESULT nscGetServerInfo(nscHSRV,
	 * PNSC_SRVINFO_DATA)</code><br> <i>native declaration :
	 * nscube_forjna.h:179</i>
	 */
	int nscGetServerInfo(Pointer hSrv, NSC_SRVINFO_DATA pSrvInfoData);

	/**
	 * Original signature : <code>nscRESULT nscFindFirstVoice(nscHSRV, const
	 * char*, int, int, int, PNSC_FINDVOICE_DATA, nscHANDLE*)</code><br>
	 * <i>native declaration : nscube_forjna.h:181</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscFindFirstVoice(com.sun.jna.Pointer, java.lang.String, int, int, int, nscube.NSC_FINDVOICE_DATA, com.sun.jna.ptr.PointerByReference)}
	 *             and
	 *             {@link #nscFindFirstVoice(com.sun.jna.Pointer, com.sun.jna.Pointer, int, int, int, nscube.NSC_FINDVOICE_DATA, com.sun.jna.ptr.PointerByReference)}
	 *             instead
	 */
	@Deprecated
	int nscFindFirstVoice(Pointer hSrv, Pointer pVoiceName, int nSampleFreq, int nLanguage,
	        int nGender, NSC_FINDVOICE_DATA pFindVoiceData, PointerByReference phVoice);

	/**
	 * Original signature : <code>nscRESULT nscFindFirstVoice(nscHSRV, const
	 * char*, int, int, int, PNSC_FINDVOICE_DATA, nscHANDLE*)</code><br>
	 * <i>native declaration : nscube_forjna.h:181</i>
	 */
	int nscFindFirstVoice(Pointer hSrv, String pVoiceName, int nSampleFreq, int nLanguage,
	        int nGender, NSC_FINDVOICE_DATA pFindVoiceData, PointerByReference phVoice);

	/**
	 * Original signature : <code>nscRESULT nscFindNextVoice(nscHANDLE,
	 * PNSC_FINDVOICE_DATA)</code><br> <i>native declaration :
	 * nscube_forjna.h:183</i>
	 */
	int nscFindNextVoice(Pointer hFindVoice, NSC_FINDVOICE_DATA pFindVoiceData);

	/**
	 * Original signature : <code>nscRESULT
	 * nscCloseFindVoice(nscHANDLE)</code><br> <i>native declaration :
	 * nscube_forjna.h:185</i>
	 */
	int nscCloseFindVoice(Pointer hFindVoice);

	/**
	 * Original signature : <code>nscRESULT
	 * nscCreateDispatcher(nscHANDLE*)</code><br> <i>native declaration :
	 * nscube_forjna.h:187</i>
	 */
	int nscCreateDispatcher(PointerByReference phDispatch);

	/**
	 * Original signature : <code>nscRESULT
	 * nscDeleteDispatcher(nscHANDLE)</code><br> <i>native declaration :
	 * nscube_forjna.h:189</i>
	 */
	int nscDeleteDispatcher(Pointer phDispatch);

	/**
	 * Original signature : <code>nscRESULT nscInitChannel(nscHSRV, const char*,
	 * int, int, nscHANDLE, nscCHANID*)</code><br> <i>native declaration :
	 * nscube_forjna.h:191</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscInitChannel(com.sun.jna.Pointer, java.lang.String, int, int, com.sun.jna.Pointer, com.sun.jna.ptr.NativeLongByReference)}
	 *             and
	 *             {@link #nscInitChannel(com.sun.jna.Pointer, com.sun.jna.Pointer, int, int, com.sun.jna.Pointer, com.sun.jna.ptr.NativeLongByReference)}
	 *             instead
	 */
	@Deprecated
	int nscInitChannel(Pointer hSrv, Pointer pVoiceList, int nSampleFreq, int nCoding,
	        Pointer pDispatch, NativeLongByReference pChId);

	/**
	 * Original signature : <code>nscRESULT nscInitChannel(nscHSRV, const char*,
	 * int, int, nscHANDLE, nscCHANID*)</code><br> <i>native declaration :
	 * nscube_forjna.h:191</i>
	 */
	int nscInitChannel(Pointer hSrv, String pVoiceList, int nSampleFreq, int nCoding,
	        Pointer pDispatch, NativeLongByReference pChId);

	/**
	 * Original signature : <code>nscRESULT nscCloseChannel(nscHSRV,
	 * nscCHANID)</code><br> <i>native declaration : nscube_forjna.h:193</i>
	 */
	int nscCloseChannel(Pointer hSrv, NativeLong ChId);

	/**
	 * Original signature : <code>nscRESULT nscLockChannel(nscHSRV, nscCHANID,
	 * nscHANDLE, nscHANDLE*)</code><br> <i>native declaration :
	 * nscube_forjna.h:195</i>
	 */
	int nscLockChannel(Pointer hSrv, NativeLong ChId, Pointer pDispatch,
	        PointerByReference phTTS);

	/**
	 * Original signature : <code>nscRESULT
	 * nscUnlockChannel(nscHANDLE)</code><br> <i>native declaration :
	 * nscube_forjna.h:197</i>
	 */
	int nscUnlockChannel(Pointer hTTS);

	/**
	 * Original signature : <code>nscRESULT nscNbVoice(nscHANDLE,
	 * int*)</code><br> <i>native declaration : nscube_forjna.h:199</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscNbVoice(com.sun.jna.Pointer, java.nio.IntBuffer)}
	 *             and
	 *             {@link #nscNbVoice(com.sun.jna.Pointer, com.sun.jna.ptr.IntByReference)}
	 *             instead
	 */
	@Deprecated
	int nscNbVoice(Pointer hTTS, IntByReference pNVoice);

	/**
	 * Original signature : <code>nscRESULT nscNbVoice(nscHANDLE,
	 * int*)</code><br> <i>native declaration : nscube_forjna.h:199</i>
	 */
	int nscNbVoice(Pointer hTTS, IntBuffer pNVoice);

	/**
	 * Original signature : <code>nscRESULT nscInfoVoice(nscHANDLE, int,
	 * PNSC_FINDVOICE_DATA)</code><br> <i>native declaration :
	 * nscube_forjna.h:201</i>
	 */
	int nscInfoVoice(Pointer hTTS, int nVoice, NSC_FINDVOICE_DATA pFindVoice);

	/**
	 * Original signature : <code>nscRESULT nscSwitchVoice(nscHANDLE,
	 * int)</code><br> <i>native declaration : nscube_forjna.h:203</i>
	 */
	int nscSwitchVoice(Pointer hTTS, int nVoice);

	/**
	 * Original signature : <code>nscRESULT nscAddText(nscHANDLE, const char*,
	 * void*)</code><br> <i>native declaration : nscube_forjna.h:205</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscAddText(com.sun.jna.Pointer, java.lang.String, com.sun.jna.Pointer)}
	 *             and
	 *             {@link #nscAddText(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer)}
	 *             instead
	 */
	@Deprecated
	int nscAddText(Pointer hTTS, Pointer pszInputTxt, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscAddText(nscHANDLE, const char*,
	 * void*)</code><br> <i>native declaration : nscube_forjna.h:205</i>
	 */
	int nscAddText(Pointer hTTS, String pszInputTxt, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscAddTextW(nscHANDLE, const
	 * wchar_t*, void*)</code><br> <i>native declaration :
	 * nscube_forjna.h:207</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscAddTextW(com.sun.jna.Pointer, com.sun.jna.WString, com.sun.jna.Pointer)}
	 *             and
	 *             {@link #nscAddTextW(com.sun.jna.Pointer, com.ochafik.lang.jnaerator.runtime.CharByReference, com.sun.jna.Pointer)}
	 *             instead
	 */
	@Deprecated
	int nscAddTextW(Pointer hTTS, CharByReference pwszInputTxt, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscAddTextW(nscHANDLE, const
	 * wchar_t*, void*)</code><br> <i>native declaration :
	 * nscube_forjna.h:207</i>
	 */
	int nscAddTextW(Pointer hTTS, WString pwszInputTxt, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscAddTextEx(nscHANDLE, const char*,
	 * const void*, const size_t, void*)</code><br> <i>native declaration :
	 * nscube_forjna.h:209</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscAddTextEx(com.sun.jna.Pointer, java.lang.String, com.sun.jna.Pointer, com.ochafik.lang.jnaerator.runtime.NativeSize, com.sun.jna.Pointer)}
	 *             and
	 *             {@link #nscAddTextEx(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, com.ochafik.lang.jnaerator.runtime.NativeSize, com.sun.jna.Pointer)}
	 *             instead
	 */
	@Deprecated
	int nscAddTextEx(Pointer hTTS, Pointer pszInputEncoding, Pointer pszInputText,
	        NativeSize sInputTextSize, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscAddTextEx(nscHANDLE, const char*,
	 * const void*, const size_t, void*)</code><br> <i>native declaration :
	 * nscube_forjna.h:209</i>
	 */
	int nscAddTextEx(Pointer hTTS, String pszInputEncoding, Pointer pszInputText,
	        NativeSize sInputTextSize, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscExecChannel(nscHANDLE,
	 * PNSC_EXEC_DATA)</code><br> <i>native declaration :
	 * nscube_forjna.h:211</i>
	 */
	int nscExecChannel(Pointer hTTS, NSC_EXEC_DATA pExecData);

	/**
	 * Original signature : <code>nscRESULT nscStartChannel(nscHANDLE,
	 * PNSC_EXEC_DATA)</code><br> <i>native declaration :
	 * nscube_forjna.h:213</i>
	 */
	int nscStartChannel(Pointer hTTS, NSC_EXEC_DATA pExecData);

	/**
	 * Original signature : <code>nscRESULT nscGetEvent(nscHANDLE,
	 * int)</code><br> <i>native declaration : nscube_forjna.h:215</i>
	 */
	int nscGetEvent(Pointer hDispatch, int nTimeOut);

	/**
	 * Original signature : <code>nscRESULT
	 * nscProcessEvent(nscHANDLE)</code><br> <i>native declaration :
	 * nscube_forjna.h:217</i>
	 */
	int nscProcessEvent(Pointer hDispatch);

	/**
	 * Original signature : <code>nscRESULT nscGetandProcess(nscHANDLE,
	 * int)</code><br> <i>native declaration : nscube_forjna.h:219</i>
	 */
	int nscGetandProcess(Pointer hDispatch, int nTimeOut);

	/**
	 * Original signature : <code>nscRESULT nscExitChannel(nscHANDLE)</code><br>
	 * <i>native declaration : nscube_forjna.h:221</i>
	 */
	int nscExitChannel(Pointer hTTS);

	/**
	 * Original signature : <code>nscRESULT
	 * nscPauseChannel(nscHANDLE)</code><br> <i>native declaration :
	 * nscube_forjna.h:223</i>
	 */
	int nscPauseChannel(Pointer hTTS);

	/**
	 * Original signature : <code>nscRESULT nscGetParamChannel(nscHANDLE,
	 * NSC_PRMTYPE_ENUM, int*)</code><br> <i>native declaration :
	 * nscube_forjna.h:225</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscGetParamChannel(com.sun.jna.Pointer, int, java.nio.IntBuffer)}
	 *             and
	 *             {@link #nscGetParamChannel(com.sun.jna.Pointer, int, com.sun.jna.ptr.IntByReference)}
	 *             instead
	 */
	@Deprecated
	int nscGetParamChannel(Pointer hTTS, int nParam, IntByReference pnParamValue);

	/**
	 * Original signature : <code>nscRESULT nscGetParamChannel(nscHANDLE,
	 * NSC_PRMTYPE_ENUM, int*)</code><br> <i>native declaration :
	 * nscube_forjna.h:225</i>
	 */
	int nscGetParamChannel(Pointer hTTS, int nParam, IntBuffer pnParamValue);

	/**
	 * Original signature : <code>nscRESULT nscSetParamChannel(nscHANDLE,
	 * NSC_PRMTYPE_ENUM, int)</code><br> <i>native declaration :
	 * nscube_forjna.h:227</i>
	 */
	int nscSetParamChannel(Pointer hTTS, int nParam, int nParamValue);

	/**
	 * Original signature : <code>nscRESULT nscServerShutdown(nscHSRV,
	 * int)</code><br> <i>native declaration : nscube_forjna.h:229</i>
	 */
	int nscServerShutdown(Pointer hSrv, int nState);

	/**
	 * Original signature : <code>nscRESULT nscSetConfigFile(const
	 * char*)</code><br> <i>native declaration : nscube_forjna.h:231</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscSetConfigFile(java.lang.String)} and
	 *             {@link #nscSetConfigFile(com.sun.jna.Pointer)} instead
	 */
	@Deprecated
	int nscSetConfigFile(Pointer lpszFileName);

	/**
	 * Original signature : <code>nscRESULT nscSetConfigFile(const
	 * char*)</code><br> <i>native declaration : nscube_forjna.h:231</i>
	 */
	int nscSetConfigFile(String lpszFileName);

	/**
	 * Original signature : <code>nscRESULT nscSetLogFile(const char*, int,
	 * int)</code><br> <i>native declaration : nscube_forjna.h:233</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscSetLogFile(java.lang.String, int, int)} and
	 *             {@link #nscSetLogFile(com.sun.jna.Pointer, int, int)} instead
	 */
	@Deprecated
	int nscSetLogFile(Pointer lpszFileName, int nLevel, int nMaxSize);

	/**
	 * Original signature : <code>nscRESULT nscSetLogFile(const char*, int,
	 * int)</code><br> <i>native declaration : nscube_forjna.h:233</i>
	 */
	int nscSetLogFile(String lpszFileName, int nLevel, int nMaxSize);

	/**
	 * Original signature : <code>nscRESULT nscAddTextUTF8(nscHANDLE, const
	 * char*, void*)</code><br> <i>native declaration :
	 * nscube_forjna.h:235</i><br>
	 * 
	 * @deprecated use the safer methods
	 *             {@link #nscAddTextUTF8(com.sun.jna.Pointer, java.lang.String, com.sun.jna.Pointer)}
	 *             and
	 *             {@link #nscAddTextUTF8(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer)}
	 *             instead
	 */
	@Deprecated
	int nscAddTextUTF8(Pointer hTTS, Pointer pszInputTxt, Pointer pUserText);

	/**
	 * Original signature : <code>nscRESULT nscAddTextUTF8(nscHANDLE, const
	 * char*, void*)</code><br> <i>native declaration : nscube_forjna.h:235</i>
	 */
	int nscAddTextUTF8(Pointer hTTS, String pszInputTxt, Pointer pUserText);
}

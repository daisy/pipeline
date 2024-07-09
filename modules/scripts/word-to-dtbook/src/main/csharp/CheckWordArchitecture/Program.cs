using Microsoft.Win32;
using System;
using System.Globalization;
using System.Text.RegularExpressions;
namespace CheckWordArchitecture
{
    internal class Program
    {
        /// <summary>
        /// Code to check for existing word bitversion
        /// (Adapted from SaveAsDAISY installer program)
        /// </summary>
        /// <returns>
        /// the bits number of word architectur (32 or 64),
        /// or 0 if word is not found
        /// </returns>
        static int CheckWord()
        {
            // If we want to check for Windows Arch, but we assume that windows is x64 as Microsoft is pushing to remove Windows x86 release.
            int archPtrBitSize = IntPtr.Size * 8; // 32 or 64, depending on executing arch

            RegistryKey lKey = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Office");
            RegistryKey wordRoot = null;
            float lastVersion = 0.0f;
            foreach (string subKey in lKey.GetSubKeyNames())
            {
                // Check if the key name is a version number
                Regex versionNumber = new Regex("[0-9]+\\.[0-9]+");
                Match result = versionNumber.Match(subKey);
                if (result.Success)
                {
                    // if it is a superior versionCheck if it has a word subkey
                    float version = float.Parse(result.Value, CultureInfo.InvariantCulture.NumberFormat);
                    if (lastVersion < version)
                    {
                        lastVersion = version;
                        RegistryKey wordKey = lKey.OpenSubKey(subKey + @"\Word\InstallRoot");
                        if (wordKey != null) wordRoot = wordKey;
                    }
                }
            }
            if(wordRoot != null)
            {
                // found a word matching the system arch
                return archPtrBitSize;
            }
            // Check for 32bits install on x64 system
            if (archPtrBitSize == 64 && wordRoot == null)
            {
                lKey = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\WOW6432Node\Microsoft\Office");
                lastVersion = 0.0f;
                foreach (string subKey in lKey.GetSubKeyNames())
                {
                    // Check if the key name is a version number
                    Regex versionNumber = new Regex("[0-9]+\\.[0-9]+");
                    Match result = versionNumber.Match(subKey);
                    if (result.Success)
                    {
                        // if it is a superior versionCheck if it has a word subkey
                        float version = float.Parse(result.Value, CultureInfo.InvariantCulture.NumberFormat);
                        if (lastVersion < version)
                        {
                            lastVersion = version;
                            RegistryKey wordKey = lKey.OpenSubKey(subKey + @"\Word\InstallRoot");
                            if (wordKey != null) wordRoot = wordKey;
                        }
                    }
                }
            }
            if(wordRoot != null)
            {
                return 32; // Found a 32bit word on 64bit system
            }
            return 0; // Not found
        }

        static int Main()
        {
            // Check if word is present on the system and return the bit version
            // as return code
            try
            {
                return CheckWord();
            } catch (Exception e)
            {
                Console.WriteLine(e.ToString());
                // If any exception is raised, we return 0 for not found
                return 0;
            }
            
        }
    }
}

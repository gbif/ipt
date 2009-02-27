/*
        Copyright (c) 2008, Adobe Systems Incorporated
        All rights reserved.

        Redistribution and use in source and binary forms, with or without 
        modification, are permitted provided that the following conditions are
        met:

    * Redistributions of source code must retain the above copyright notice, 
        this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
                notice, this list of conditions and the following disclaimer in the 
        documentation and/or other materials provided with the distribution.
    * Neither the name of Adobe Systems Incorporated nor the names of its 
        contributors may be used to endorse or promote products derived from 
        this software without specific prior written permission.

        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
        IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
        THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
        PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
        CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
        EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
        PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
        PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
        LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
        NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.google.maps.extras.xmlparsers
{

	import mx.utils.StringUtil;

	/**
	 * Utility class with functions to help parse XML  feeds.
	 */	
	public class ParsingTools
	{
		/**
		 * Checks to see if a piece of XML that should be a string is empty.
		 * If so, returns null.
		 * 
		 * @param x The piece of XML to test.
		 * @return null if the string is emply, otherwise the value of the XML
		 *         node.
		 */
		public static function nullCheck(x:XMLList):String
		{
			var s:String = String(x);
			if (StringUtil.trim(s).length == 0)
			{
				return null;
			}
			return s;
		}

		/**
		 * Checks to see if a piece of XML that should be a number is empty.
		 * If so, returns NaN.
		 * 
		 * @param x The piece of XML to test.
		 * @return null if the number is emply, otherwise the value of the XML
		 *         node.
		 */
		public static function nanCheck(x:XMLList):Number
		{
			var s:String = String(x);
			if (ParsingTools.nullCheck(x) == null || s.search(/\d/) == -1)
			{
				return NaN;
			}
			return Number(s);
		}


		/**
		 * Checks to see if a piece of XML that should be a date is empty.
		 * If so, returns null. If not, parses the string into a date using
		 * the specifid date parsing function.
		 * 
		 * @param x The piece of XML to test.
		 * @param f The date parsing function to use to parse the string if
		 *          it's not null.
		 * @return null if the date is emply, otherwise a date object created
		 *         using the specified date-parsing function.
		 */
		public static function dateCheck(x:XMLList, f:Function):Date
		{
			var s:String = String(x);
			if (ParsingTools.nullCheck(x) == null)
			{
				return null;
			}
			return f(s);
		}   
	}
}

package jwiki.core;

import static jwiki.commons.Commons.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import jwiki.commons.CStrings;
import jwiki.commons.Commons;
import jwiki.commons.WikiGen;
import jwiki.mbot.WAction;
import jwiki.util.FLogin;
import jwiki.util.FString;
import jwiki.util.FSystem;
import jwiki.util.ReadFile;

public class BC
{
	public static void main(String[] args) throws Throwable
	{
		System.out.println(FLogin.getPXFor("Fastily"));
	}
}
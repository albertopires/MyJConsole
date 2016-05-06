/*
 * Copyright (C) 2016 Alberto Pires de Oliveira Neto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.albertopires.mjc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	public static void main(String[] args) {
		if ((args.length != 1)) {
			System.err.println("JMX Monitor v1.0.1 - 06/May/2016");
			System.err.println("Parameters: <config_file>\n");
			System.err.println("Fields:");
			System.err.println("0 - TimeStamp");
			System.err.println("1 - CPU Usage");
			System.err.println("2 - Heap Usage");
			System.err.println("3 - Loaded Class Count");
			System.err.println("4 - Thread Count");
			System.err.println("5 - CMS Usage");
			System.err.println("6 - Par Eden Usage");
			System.err.println("7 - Non-Heap Usage");
			System.err.println("8 - CMS Usage Threshold Count\n");
			System.exit(1);
		}

		Properties jvmToMonitor = loadConfig(args[0]);
		int i = 0;
		String host, port, logDir;
		LogThread li;
		while (true) {
			host = jvmToMonitor.getProperty("host." + i);
			port = jvmToMonitor.getProperty("port." + i);
			logDir = jvmToMonitor.getProperty("dir." + i);
			i++;
			System.err.println("Host " + host + ":" + port);
			if (host == null)
				break;
			li = new LogThread(host, port, logDir, jvmToMonitor);
			new Thread(li).start();
		}
	}

	private static Properties loadConfig(String confFile) {
		File f = new File(confFile);

		Properties p = new Properties();

		try {
			FileInputStream fi = new FileInputStream(f);
			p.load(fi);
			return p;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

}

class LogThread implements Runnable {

	private String host;
	private String port;
	private String logDir;
	private Properties conf;

	public LogThread(String host, String port, String logDir, Properties conf) {
		this.host = host;
		this.port = port;
		this.logDir = logDir;
		this.conf = conf;
	}

	@Override
	public void run() {
		String[] params = new String[3];
		params[0] = host;
		params[1] = port;
		params[2] = logDir;
		try {
			JConsoleM.jvmLog(params, conf);
		} catch (Exception ex) {
			System.err.println("Thread Exception " + ex.getMessage());
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}
}

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

import java.util.ArrayList;
import java.util.Properties;

public class BeanConf {
	public static final int NAME = 0;
	public static final int ATTR0 = 1;
	public static final int ATTR1 = 2;

	private ArrayList<String[]> beans;
	private Properties beanConf;

	public BeanConf(Properties beanConf) {
		this.beans = new ArrayList<String[]>();
		this.beanConf = beanConf;

		loadBeanConf();
	}

	private void loadBeanConf() {
		int i = 0;

		while(true) {
			String pair[] = new String[3];
			pair[NAME] = beanConf.getProperty(i+".name");
			if (pair[NAME] == null) break;
			pair[ATTR0] = beanConf.getProperty(i+".attr0");
			if (pair[ATTR0] == null) break;
			pair[ATTR1] = beanConf.getProperty(i+".attr1");
			beans.add(pair);
			i++;
		}
	}

	public ArrayList<String[]> getBeans() {
		return beans;
	}

	public void setBeans(ArrayList<String[]> beans) {
		this.beans = beans;
	}

}

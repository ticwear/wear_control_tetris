/*
 * Copyright 2013 Simon Willeke
 * contact: hamstercount@hotmail.com
 */

/*
    This file is part of Blockinger.

    Blockinger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Blockinger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Blockinger.  If not, see <http://www.gnu.org/licenses/>.

 */

package com.mobvoi.knowledgegraph.wearcontroltetris.components;

import com.mobvoi.knowledgegraph.wearcontroltetris.activities.GameActivity;

public abstract class Component {

	protected GameActivity host;
	
	public Component(GameActivity ga) {
		host = ga;
	}

	public void reconnect(GameActivity ga) {
		host = ga;
	}

	public void disconnect() {
		host = null;
	}
	
}

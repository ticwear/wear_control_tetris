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

package com.mobvoi.knowledgegraph.wearcontroltetris.pieces;

import android.content.Context;

import com.mobvoi.knowledgegraph.wearcontroltetris.Square;

public class JPiece extends Piece3x3 {

	private Square jSquare;
	
	public JPiece(Context c) {
		super(c);
		jSquare = new Square(type_J,c);
		pattern[1][0] = jSquare;
		pattern[1][1] = jSquare;
		pattern[1][2] = jSquare;
		pattern[2][2] = jSquare;
		reDraw();
	}

	@Override
	public void reset(Context c) {
		super.reset(c);
		pattern[1][0] = jSquare;
		pattern[1][1] = jSquare;
		pattern[1][2] = jSquare;
		pattern[2][2] = jSquare;
		reDraw();
	}

}

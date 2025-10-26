/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ast;

/**
 * An exception that represents an evaluation problem.
 * 
 * @author Zach Kissel
 */
public class EvaluationException extends Exception {
    /**
     * The constructor for an evaluation exception that
     * prints the line number and a statement of failure.
     */
    public EvaluationException() {
        super("Interpretation failed.");
    }

    // Add this constructor to define EvaluationException(String)
    public EvaluationException(String message) {
        super(message);
    }
}

/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.shell;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MValidator;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "shell",
        name = "range",
        description = "Print a range of outputs numbers or lower case characters")
@Service
public class CmdRange extends AbstractCmd {

    @Argument(
            index = 0,
            name = "from",
            required = true,
            description = "From -n .. +n or abc ",
            multiValued = false)
    String from;

    @Argument(
            index = 1,
            name = "to",
            required = true,
            description = "To  -n .. +n or abc ",
            multiValued = false)
    String to;

    @Argument(
            index = 2,
            name = "step",
            required = false,
            description = "Step -n .. +n",
            multiValued = false)
    String step;

    @Option(
            name = "-j",
            aliases = {"--join"},
            description = "Glue between values",
            required = false,
            multiValued = false)
    String join = "\n";

    @Option(
            name = "-o",
            aliases = {"--out"},
            description = "Print to sys out",
            required = false,
            multiValued = false)
    boolean toOut = false;

    //    @Reference
    //    private Session session;

    @Override
    public Object execute2() throws Exception {

        StringWriter sw = toOut ? null : new StringWriter();
        @SuppressWarnings("resource")
        PrintWriter out = toOut ? new PrintWriter(System.out) : new PrintWriter(sw);

        if (MValidator.isInteger(from) && MValidator.isInteger(to)) {
            int f = MCast.toint(from, 0);
            int t = MCast.toint(to, 0);
            int s = f < t ? 1 : -1;
            s = MCast.toint(step, s);
            if (s == 0 || f < t && s < 0 || f > t && s > 0) {
                System.err.println("Invalid step value");
                return null;
            }
            int c = f;
            while (true) {
                out.print(c);
                out.print(join);
                c = c + s;
                if (s > 0 && c > t || s < 0 && c < t) break;
            }
        } else if (MValidator.isNumber(from) && MValidator.isNumber(to)) {
            double f = MCast.todouble(from, 0);
            double t = MCast.todouble(to, 0);
            double s = f < t ? 1 : -1;
            s = MCast.todouble(step, s);
            if (s == 0 || f < t && s < 0 || f > t && s > 0) {
                System.err.println("Invalid step value");
                return null;
            }
            double c = f;
            while (true) {
                out.print(c);
                out.print(join);
                c = c + s;
                if (s > 0 && c > t || s < 0 && c < t) break;
            }
        } else if (from.matches("^[a-z]*$") && to.matches("^[a-z]*$")) {
            // find default step value
            int comp = compareTo(to, from);
            int s = comp;
            if (s == 0) s = 1;
            else if (s < 0) s = -1;
            else s = 1;
            s = MCast.toint(step, s);
            // check step direction
            if (s == 0 || comp > 0 && s < 0 || comp < 0 && s > 0) {
                System.err.println("Invalid step value");
                return null;
            }
            // convert 'from' to a char array to allow inc / dec
            char[] c = new char[Math.max(from.length(), to.length()) + 1];
            for (int i = 0; i < c.length; i++)
                if (from.length() > i) c[i] = from.charAt(i);
                else c[i] = 0;

            // loop
            String cStr = toString(c);
            while (true) {
                out.print(cStr);
                out.print(join);
                if (s > 0) {
                    for (int i = 0; i < s; i++) increment(c, 0);
                    cStr = toString(c);
                    if (compareTo(to, cStr) < 0) break;
                } else {
                    for (int i = 0; i < -s; i++) decrement(c, 0);
                    cStr = toString(c);
                    if (compareTo(to, cStr) > 0) break;
                }
            }
        } else {
            System.err.println("Invalid range");
        }

        out.flush();
        if (sw != null) {
            return sw.toString();
        } else return null;
    }

    private int compareTo(String o1, String o2) {
        if (o1.length() < o2.length()) return -1;
        if (o2.length() < o1.length()) return 1;
        return o1.compareTo(o2);
    }

    private String toString(char[] c) {
        int l = length(c);
        char[] o = new char[l];
        for (int i = 0; i < l; i++) o[i] = c[l - i - 1];
        return new String(o);
    }

    private int length(char[] c) {
        for (int i = 0; i < c.length; i++) if (c[i] == 0) return i;
        return c.length;
    }

    private void increment(char[] c, int pos) {
        if (pos >= c.length) return;
        char x = c[pos];
        if (x == 0) x = 'a';
        else x++;
        if (x > 'z') {
            x = 'a';
            increment(c, pos + 1);
        }
        c[pos] = x;
    }

    private void decrement(char[] c, int pos) {
        if (pos >= c.length) return;
        char x = c[pos];
        if (x == 0) x = 'z';
        else x--;
        if (x < 'a') {
            x = 'z';
            decrement(c, pos + 1);
        }
        c[pos] = x;
    }
}

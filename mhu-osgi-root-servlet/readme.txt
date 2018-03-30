====
    Copyright 2018 Mike Hummel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

feature:install http-whiteboard
install -s mvn:de.mhus.osgi/mhu-osgi-root-servlet/1.3.0-SNAPSHOT


etc/rootservlet.properties

default.redirect=/home
rule0=/console
rule0.redirect=/system/console
rule1=.*/favicon.ico
rule1.error=404
rule1.errormsg=favicon not supported


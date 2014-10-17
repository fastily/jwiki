/**
 * A simple multi-threaded bot framework to make mass changes or perform analytics extremely quickly. This package
 * contains a driver (<code>MBot.java</code>) and an object representing an entity it acts upon (<code>WAction.java</code>). To
 * use MBot, you need to extend WAction, and implement the task you would like to perform in <code>doJob()</code>. MBot
 * contains a ThreadManager, which will spawn threads accordingly to consume any WActions you have created and
 * save/process the results. If you're doing analytics, it is recommended that you create a field in each WAction
 * extension, set this field in <code>doJob()</code> depending on the result of your analysis, and then loop through the
 * WActions you created after calling your MBot's <code>start().</code>
 */

package jwiki.mbot;
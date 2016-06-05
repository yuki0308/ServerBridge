/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Valentin 'ThisIsMac' Marchaud
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package fr.vmarchaud.mineweb.bungee;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.injector.JSONAPIChannelDecoder;
import fr.vmarchaud.mineweb.common.injector.NettyInjector;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.netty.HandlerBoss;

public class BungeeNettyInjector extends NettyInjector {
	
	private ICore			api;
	
	public BungeeNettyInjector(ICore api) {
		this.api = api;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void inject() {
        if (injected)
            throw new IllegalStateException("Cannot inject twice.");
        try {
            Class<?> server = api.getServer().getClass();
            
            Field listenersList = server.getDeclaredField("listeners");
            listenersList.setAccessible(true);
            
            HashSet<Channel> listeners = (HashSet<Channel>)listenersList.get(api.getServer());
            
            for(Channel ch : listeners) {
            	System.out.println(ch.pipeline().first());
            	injectChannel(ch);
                System.out.println("succesfuly injected");
            	
            }
            
            injected = true;
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject channel futures.", e);
        }
    }

	@Override
	protected void injectChannel(final Channel channel) {
		channel.pipeline().forEach(System.out::println);
		
		channel.pipeline().addFirst(new JSONAPIChannelDecoder(api));
		
    	System.out.println(channel.pipeline().first());
    	
    	ProxyServer	server = (ProxyServer) api.getServer();
    	server.getScheduler().schedule((Plugin) api.getPlugin(), new Runnable(){

			@Override
			public void run() {
		    	channel.disconnect();
			}
    		
    	}, 10, TimeUnit.SECONDS);
	}

}
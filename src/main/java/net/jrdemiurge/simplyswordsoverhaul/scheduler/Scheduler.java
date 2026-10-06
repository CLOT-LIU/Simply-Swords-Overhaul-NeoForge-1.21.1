package net.jrdemiurge.simplyswordsoverhaul.scheduler;

import net.jrdemiurge.simplyswordsoverhaul.SimplySwordsOverhaul;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = SimplySwordsOverhaul.MOD_ID)
public class Scheduler {
    private static final List<SchedulerTask> tasks = new ArrayList<>();

    public static void schedule(Runnable task, int delay, int period) {
        synchronized (tasks) {
            tasks.add(new SchedulerTask(delay, period, task));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        synchronized (tasks) {
            Iterator<SchedulerTask> iterator = tasks.iterator();
            while (iterator.hasNext()) {
                SchedulerTask st = iterator.next();
                int newTicks = st.getTicksRemaining() - 1;
                st.setTicksRemaining(newTicks);
                if (newTicks <= 0) {
                    st.getTask().run();
                    if (st.isRepeating()) {
                        st.setTicksRemaining(st.getPeriod());
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
    }
}

package multithread.threads;

import core.RequestHandler;
import network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Task;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ProcessThread.class);

    private final BlockingQueue<Task> processQueue;
    private final BlockingQueue<Response> responseQueue;
    private final RequestHandler requestHandler;

    public ProcessThread(BlockingQueue<Task> processQueue, BlockingQueue<Response> responseQueue, RequestHandler requestHandler) {
        this.processQueue = processQueue;
        this.responseQueue = responseQueue;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = processQueue.take();
                logger.debug("Поток {} берет задачу из очереди", Thread.currentThread().getName());
                Response response = requestHandler.handle(task.request());
                Response responseWithAddress = new Response.Builder(response).host(((InetSocketAddress) task.address()).getHostName())
                        .port(((InetSocketAddress) task.address()).getPort()).build();

                responseQueue.put(responseWithAddress);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.debug("Поток выполнения задач {} закрылся", Thread.currentThread().getName());
    }
}

package com.tsup.filters;

import com.tsup.protocol.Flags;
import com.tsup.protocol.Segment;

import java.util.Iterator;

/**
 * Фильтр одновременно проверяет ACK и запускает timeout в случае долгого
 * отсутствия ACK, а также самостоятельно отправляет ACK на каждый 8 запрос
 * */
public class AckFilter implements SegmentFilter {
    /** Количество не пришедших ack */
    //private int missedAckCount = 0;
    /** В случае если более limitMissedAck ack не придет, то вызываем timeout и disconnect */
    //private final int limitMissedAck = 10;
    /** Настраиваемое значение на какой N-запрос отправлять ack */
    private final int ackN = 8;

    @Override
    public void handle(Segment segment, FilterContext fContext,
                       Iterator<SegmentFilter> iterator) throws Exception {

        //Отправляем ACK на каждый 8 запрос.
        if (segment.seq % ackN == 0)
            fContext.getContext().sendAck();

        //Уведомляем что ack пришел.
        if (segment.flags.has(Flags.ACK_BITFIELD)) {
            fContext.getConnectionContext().getAckMonitor().ackReceived();
System.err.println("ACK " + segment.seq);
        }

        //передадим то что ACK дошел успешно и никаких ошибок timeout нет
        /*if (segment.flags.has(Flags.ACK_BITFIELD)) {
            //ack пришел. Обработано удачно. Сбрасываем ожидание...
            missedAckCount = 0;
        } else if (fContext.getContext().getSeq() % ackN == 0) { //на каждый 8 отправленный запрос регистрируем сколько ack должно придти.

             //если должен был прийти ack, а его нет, то увеличиваем кол-во не пришедших ack.
            missedAckCount++;

            if (missedAckCount >= limitMissedAck) {
                //протокол timeout
                fContext.noticeDisconnectHandler("timeout");
                return;
            }
        }*/

        fContext.next(segment, iterator);
    }


}

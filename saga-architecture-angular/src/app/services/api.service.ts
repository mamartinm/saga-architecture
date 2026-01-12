import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, switchMap, startWith, catchError, of, Subject, merge } from 'rxjs';

export interface UserBalance {
  userId: number;
  balance: number;
}

export interface OrderRequest {
  userId: number;
  productId: number;
  amount: number;
  orderId?: string | null;
}

export interface OrderResponse {
  id: string;
  userId: number;
  productId: number;
  price: number;
  orderStatus: string;
}

export interface SagaEvent {
  time: Date;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly ORDER_SERVICE_URL = 'http://localhost:8080';
  private readonly PAYMENT_SERVICE_URL = 'http://localhost:8081';

  // Signals
  private balanceSignal = signal<UserBalance | null>(null);
  private loadingSignal = signal<boolean>(false);
  private errorSignal = signal<string | null>(null);
  private eventsSignal = signal<SagaEvent[]>([]);

  // Trigger para forzar actualización
  private refreshTrigger = new Subject<void>();

  // Exponer como readonly
  readonly balance = this.balanceSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly events = this.eventsSignal.asReadonly();

  constructor(private http: HttpClient) { }

  /**
   * Inicia el polling automático del saldo cada N segundos
   */
  startBalancePolling(userId: number, intervalMs: number = 2000) {
    // Combina el intervalo regular con el trigger manual
    merge(
      interval(intervalMs).pipe(startWith(0)),
      this.refreshTrigger
    ).pipe(
      switchMap(() => {
        this.loadingSignal.set(true);
        return this.http.get<UserBalance>(`${this.PAYMENT_SERVICE_URL}/payments/balance/${userId}`).pipe(
          catchError(err => {
            this.errorSignal.set('Error al obtener el saldo');
            console.error('Error fetching balance:', err);
            return of(null);
          })
        );
      })
    ).subscribe(balance => {
      this.loadingSignal.set(false);
      if (balance) {
        this.balanceSignal.set(balance);
        this.errorSignal.set(null);
      }
    });
  }

  /**
   * Fuerza una actualización inmediata del saldo
   */
  refreshBalance() {
    this.refreshTrigger.next();
  }

  /**
   * Crea un nuevo pedido
   */
  async createOrder(order: OrderRequest): Promise<OrderResponse> {
    this.loadingSignal.set(true);
    this.addEvent(`Iniciando pedido para producto ${order.productId}...`, 'info');

    try {
      const response = await this.http.post<OrderResponse>(
        `${this.ORDER_SERVICE_URL}/orders`,
        order
      ).toPromise();

      this.addEvent(`Pedido ${response?.id.substring(0, 8)} creado. Estado: ${response?.orderStatus}`, 'success');

      // Simular seguimiento de la saga (polling de saldo ayuda a ver cambios)
      if (order.productId === 102) {
        this.addEvent('Detectado producto sin stock. Esperando compensación...', 'warning');
      } else {
        this.addEvent('Procesando pago e inventario...', 'info');
      }

      // Actualiza el saldo después de crear el pedido
      setTimeout(() => this.refreshBalance(), 1000);
      setTimeout(() => this.refreshBalance(), 3000); // Doble check para ver el efecto

      return response!;
    } catch (err) {
      this.addEvent('Error en la comunicación con el servicio de pedidos', 'error');
      throw err;
    } finally {
      this.loadingSignal.set(false);
    }
  }

  private addEvent(message: string, type: 'info' | 'success' | 'warning' | 'error') {
    const newEvent: SagaEvent = { time: new Date(), message, type };
    this.eventsSignal.update(events => [newEvent, ...events].slice(0, 10));
  }
}

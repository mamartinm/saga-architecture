import { Component } from '@angular/core';
import { BalanceComponent } from './components/balance/balance.component';
import { OrderFormComponent } from './components/order-form/order-form.component';
import { SagaLogComponent } from './components/saga-log/saga-log.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BalanceComponent, OrderFormComponent, SagaLogComponent],
  template: `
    <div class="app-container">
      <header class="app-header">
        <div class="logo">
          <span class="logo-icon">🔄</span>
          <span class="logo-text">Saga Architecture</span>
        </div>
        <div class="header-badge">
          <span class="badge">Demo</span>
        </div>
      </header>
      
      <main class="app-main">
        <div class="hero-section">
          <h1>Patrón Saga Orquestado</h1>
          <p>Demostración de microservicios con compensación automática</p>
        </div>
        
        <div class="cards-grid">
          <div class="column">
            <app-balance></app-balance>
            <div class="spacer"></div>
            <app-order-form></app-order-form>
          </div>
          
          <div class="column">
            <app-saga-log></app-saga-log>
          </div>
        </div>
        
        <div class="info-section">
          <div class="info-card">
            <h4>📖 ¿Cómo funciona?</h4>
            <ol>
              <li>Crea un pedido con el formulario</li>
              <li>El saldo se actualiza automáticamente</li>
              <li>Si el inventario falla, el pago se revierte</li>
              <li>Observa la compensación en tiempo real</li>
            </ol>
          </div>
          
          <div class="info-card">
            <h4>🧪 Casos de Prueba</h4>
            <ul>
              <li><strong>Producto 101:</strong> Flujo exitoso (hay stock)</li>
              <li><strong>Producto 102:</strong> Rollback (sin stock)</li>
              <li><strong>Monto > 1000:</strong> Pago rechazado (saldo insuficiente)</li>
            </ul>
          </div>
        </div>
      </main>
      
      <footer class="app-footer">
        <p>💡 Conectado a: Order Service (8080) | Payment Service (8081) | Inventory Service (8082)</p>
      </footer>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: linear-gradient(180deg, #0f0f1a 0%, #1a1a2e 50%, #16213e 100%);
    }
    
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    
    .app-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 40px;
      background: rgba(0, 0, 0, 0.3);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }
    
    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .logo-icon {
      font-size: 2rem;
    }
    
    .logo-text {
      font-size: 1.4rem;
      font-weight: 700;
      background: linear-gradient(90deg, #00d9ff, #00ff88);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .header-badge .badge {
      background: rgba(0, 217, 255, 0.2);
      color: #00d9ff;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 500;
    }
    
    .app-main {
      flex: 1;
      padding: 40px;
      max-width: 1200px;
      margin: 0 auto;
      width: 100%;
      box-sizing: border-box;
    }
    
    .hero-section {
      text-align: center;
      margin-bottom: 48px;
      color: white;
    }
    
    .hero-section h1 {
      font-size: 2.5rem;
      font-weight: 700;
      margin: 0 0 12px 0;
      background: linear-gradient(90deg, #ffffff, #00d9ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .hero-section p {
      font-size: 1.1rem;
      color: rgba(255, 255, 255, 0.6);
      margin: 0;
    }
    
    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 32px;
      margin-bottom: 48px;
    }
    
    .column {
      display: flex;
      flex-direction: column;
      gap: 32px;
    }
    
    .spacer {
      height: 0;
    }
    
    .info-section {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }
    
    .info-card {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 16px;
      padding: 24px;
      color: white;
    }
    
    .info-card h4 {
      margin: 0 0 16px 0;
      font-size: 1rem;
      font-weight: 600;
    }
    
    .info-card ol, .info-card ul {
      margin: 0;
      padding-left: 20px;
      color: rgba(255, 255, 255, 0.7);
      line-height: 1.8;
    }
    
    .info-card strong {
      color: #00d9ff;
    }
    
    .app-footer {
      text-align: center;
      padding: 20px;
      color: rgba(255, 255, 255, 0.4);
      font-size: 0.85rem;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
    }
    
    .app-footer p {
      margin: 0;
    }
  `]
})
export class AppComponent {
  title = 'Saga Architecture';
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Subscription, forkJoin } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { LayoutService } from 'src/app/layout/service/app.layout.service';
import { ProjectService } from 'src/app/services/project.service';
import { StatisticsService } from 'src/app/services/statistics.service';
import { VersionService } from 'src/app/services/version.service';

@Component({
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {

  items!: MenuItem[];
  subscription!: Subscription;

  basicData: any;
  stackedData: any;
  lineData: any;
  pieData: any;
  radarData: any;
  polarAreaData: any;
  coverageData: any;
  comboData: any;

  chartOptions: any;
  stackedChartOptions: any;
  statistics: any = {};

  doughnutChartOptions: any;
  pieChartOptions: any;
  lineChartOptions: any;
  CoveredNbTotal: number;
  TestsNbTotal: number;

  constructor(private projectService: ProjectService, private versionService: VersionService, private statisticsService: StatisticsService, public layoutService: LayoutService) {
    this.subscription = this.layoutService.configUpdate$
      .pipe(debounceTime(25))
      .subscribe(() => {
        this.initChartOptions();
      });
  }

  ngOnInit() {
    this.initChartOptions();
    this.loadStatistics();
    this.loadVersionsAndExecutionData();

    this.items = [
      { label: 'Ajouter Nouveau', icon: 'pi pi-fw pi-plus' },
      { label: 'Supprimer', icon: 'pi pi-fw pi-minus' }
    ];
  }

  initChartOptions() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.chartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }
      }
    };

    this.stackedChartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          stacked: true,
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          stacked: true,
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }
      }
    };

    this.doughnutChartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        },
        datalabels: {
          display: (context) => context.dataset.data.length === 1,
          align: 'center',
          anchor: 'center',
          formatter: (value, context) => {
            let sum = 0;
            const dataArr = context.chart.data.datasets[0].data;
            dataArr.map(data => {
              sum += data;
            });
            return sum;
          },
          color: textColor,
          font: {
            weight: 'bold',
            size: 20,
          }
        },
        title: {
          display: true,
          text: 'Couverture',
          color: textColor,
          font: {
            size: 16
          }
        }
      },
      cutout: '40%',
    };

    this.pieChartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        },
        datalabels: {
          formatter: (value, context) => {
            let sum = 0;
            const dataArr = context.chart.data.datasets[0].data;
            dataArr.map(data => {
              sum += data;
            });
            return sum;
          },
          color: textColor,
          font: {
            weight: 'bold',
            size: 20,
          }
        },
        title: {
          display: true,
          text: 'Tests',
          color: textColor,
          font: {
            size: 16
          }
        }
      },
      cutout: '40%',
    };

    this.lineChartOptions = {
      responsive: true,
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        },
        tooltip: {
          mode: 'index',
          intersect: false,
        },
        title: {
          display: true,
          text: 'Versions et Exécutions par Mois',
          color: textColor,
          font: {
            size: 16
          }
        }
      },
      interaction: {
        mode: 'index',
        intersect: false,
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }
      },
      elements: {
        point: {
          radius: 5,
          hoverRadius: 7,
        },
        line: {
          tension: 0.4
        }
      }
    };
  }

  loadStatistics() {
    this.statisticsService.getStatistics().subscribe(data => {
      this.statistics = data;
    });
  }

  loadVersionsAndExecutionData() {
    this.versionService.getCurrentUserVersions().subscribe(versions => {
      const executionRequests = [];

      versions.forEach(version => {
        version.executions.forEach(execution => {
          executionRequests.push(this.projectService.getResultPostmanCollectionByexecution(execution.id));
        });
      });

      forkJoin(executionRequests).subscribe(responses => {
        const allStats = responses.map(response => this.readResponseAsJson(response));

        Promise.all(allStats).then(statsObjects => {
          const combinedStats = this.combineStats(statsObjects, versions);

          const processedData = this.processStatsData(combinedStats);

          this.basicData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Assertions Totales',
                data: processedData.totalAssertions,
                backgroundColor: '#2196f3'
              },
              {
                label: 'Assertions Échouées Totales',
                data: processedData.totalFailedAssertions,
                backgroundColor: '#9c27b0'
              }
            ]
          };

          this.stackedData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Requêtes Totales',
                data: processedData.requestsTotal,
                backgroundColor: '#009688',
                stack: 'a'
              },
              {
                label: 'Requêtes Échouées Totales',
                data: processedData.requestsFailedTotal,
                backgroundColor: '#FFA726',
                stack: 'a'
              }
            ]
          };

          this.lineData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Versions par Mois',
                data: processedData.versionsPerMonth,
                fill: false,
                borderColor: '#2196f3',
                pointBackgroundColor: '#2196f3',
                tension: 0.4
              },
              {
                label: 'Exécutions par Mois',
                data: processedData.executionsPerMonth,
                fill: false,
                borderColor: '#9c27b0',
                pointBackgroundColor: '#9c27b0',
                tension: 0.4
              }
            ]
          };

          this.TestsNbTotal = combinedStats.totalAssertions;

          this.pieData = {
            labels: ['Tests Réussis', 'Tests Échoués'],
            datasets: [
              {
                data: [
                  combinedStats.totalAssertions - combinedStats.totalFailedAssertions,
                  combinedStats.totalFailedAssertions
                ],
                backgroundColor: ['#009688', '#9c27b0']
              }
            ]
          };

          this.radarData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Temps de Réponse',
                data: processedData.responseTimes,
                borderColor: '#2196f3',
                backgroundColor: 'rgba(66,165,245,0.2)'
              }
            ]
          };

          this.polarAreaData = {
            labels: ['Assertions Totales', 'Assertions Échouées Totales', 'Requêtes Totales', 'Requêtes Échouées Totales'],
            datasets: [
              {
                data: [
                  combinedStats.totalAssertions,
                  combinedStats.totalFailedAssertions,
                  combinedStats.requestsTotal,
                  combinedStats.requestsFailedTotal
                ],
                backgroundColor: ['#2196f3', '#9c27b0', '#009688', '#FFA726']
              }
            ]
          };

          this.coverageData = {
            labels: ['Requêtes Couvertes', 'Requêtes Non Couvertes'],
            datasets: [
              {
                data: [processedData.coveredRequests, processedData.notCoveredRequests],
                backgroundColor: ['#009688', '#9c27b0']
              }
            ]
          };

          this.comboData = {
            labels: processedData.labels,
            datasets: [
              {
                type: 'line',
                label: 'Temps de Réponse',
                data: processedData.responseTimes,
                borderColor: '#2196f3',
                backgroundColor: 'rgba(66,165,245,0.2)',
                fill: false
              },
              {
                type: 'bar',
                label: 'Requêtes Totales',
                data: processedData.requestsTotal,
                backgroundColor: '#009688'
              },
              {
                type: 'bar',
                label: 'Requêtes Échouées Totales',
                data: processedData.requestsFailedTotal,
                backgroundColor: '#FFA726'
              }
            ]
          };
        });
      });
    });
  }

  calculatePassRequests(assertions: any[]): boolean {
    if (!assertions || assertions.length === 0) {
      return false;
    }
    const passedAssertions = assertions.filter(assertion => !assertion.error).length;
    return passedAssertions === assertions.length;
  }

  readResponseAsJson(response: any): Promise<any> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const jsonObject = JSON.parse(reader.result as string);
          resolve(jsonObject);
        } catch (error) {
          reject(error);
        }
      };
      reader.onerror = () => reject(reader.error);
      const blob = new Blob([response], { type: 'application/json' });
      reader.readAsText(blob);
    });
  }

  combineStats(statsObjects: any[], versions: any[]): any {
    const combinedStats = {
      totalAssertions: 0,
      totalFailedAssertions: 0,
      requestsTotal: 0,
      requestsFailedTotal: 0,
      responseTimes: [],
      responseSizes: [],
      monthlyAssertions: {},
      monthlyFailedAssertions: {},
      coveredRequests: 0,
      notCoveredRequests: 0,
      monthlyVersions: {},
      monthlyExecutions: {}
    };

    statsObjects.forEach((stats, index) => {
      const version = versions[index];
      if (version && version.createdAt) {
        const month = this.convertTimestampToMonth(new Date(version.createdAt).getTime());

        if (!combinedStats.monthlyVersions[month]) {
          combinedStats.monthlyVersions[month] = 0;
          combinedStats.monthlyExecutions[month] = 0;
        }

        combinedStats.monthlyVersions[month] += 1;
        combinedStats.monthlyExecutions[month] += stats.run.executions.length;

        if (!combinedStats.monthlyAssertions[month]) {
          combinedStats.monthlyAssertions[month] = 0;
          combinedStats.monthlyFailedAssertions[month] = 0;
        }

        combinedStats.monthlyAssertions[month] += stats.run.stats.assertions.total;
        combinedStats.monthlyFailedAssertions[month] += stats.run.stats.assertions.failed;

        combinedStats.totalAssertions += stats.run.stats.assertions.total;
        combinedStats.totalFailedAssertions += stats.run.stats.assertions.failed;
        combinedStats.requestsTotal += stats.run.stats.requests.total;
        combinedStats.requestsFailedTotal += stats.run.stats.requests.failed;

        stats.run.executions.forEach((execution: any) => {
          combinedStats.responseTimes.push(execution.timings?.responseTime || 0);
          combinedStats.responseSizes.push(execution.timings?.responseSize || 0);

          if (this.calculatePassRequests(execution.assertions)) {
            combinedStats.coveredRequests++;
          } else {
            combinedStats.notCoveredRequests++;
          }
        });
      }
    });

    return combinedStats;
  }

  convertTimestampToMonth(timestamp: number): string {
    const date = new Date(timestamp);
    return date.toLocaleString('fr-FR', { month: 'short' });
  }

  processStatsData(combinedStats: any): any {
    const currentDate = new Date();
    const labels = [];

    for (let i = 6; i >= 0; i--) {
      const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1);
      labels.push(this.convertTimestampToMonth(date.getTime()));
    }

    const versionsPerMonth = labels.map(month => combinedStats.monthlyVersions[month] || 0);
    const executionsPerMonth = labels.map(month => combinedStats.monthlyExecutions[month] || 0);
    this.CoveredNbTotal = 0;
    this.CoveredNbTotal += (combinedStats.coveredRequests + combinedStats.notCoveredRequests);

    return {
      labels,
      versionsPerMonth,
      executionsPerMonth,
      totalAssertions: labels.map(month => combinedStats.monthlyAssertions[month] || 0),
      totalFailedAssertions: labels.map(month => combinedStats.monthlyFailedAssertions[month] || 0),
      requestsTotal: labels.map(month => combinedStats.requestsTotal),
      requestsFailedTotal: labels.map(month => combinedStats.requestsFailedTotal),
      responseTimes: combinedStats.responseTimes,
      responseSizes: combinedStats.responseSizes,
      coveredRequests: combinedStats.coveredRequests,
      notCoveredRequests: combinedStats.notCoveredRequests
    };
  }

  calculateCoveragePercentage(totalAssertions: number, totalFailedAssertions: number): number {
    const passedAssertions = totalAssertions - totalFailedAssertions;
    return (passedAssertions / totalAssertions) * 100;
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  convertTimestampToDate(timestamp: number): string {
    const date = new Date(timestamp);
    const options: Intl.DateTimeFormatOptions = {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    };
    return date.toLocaleDateString('fr-FR', options) + ' ' + date.toLocaleTimeString('fr-FR', { hour12: false });
  }
}

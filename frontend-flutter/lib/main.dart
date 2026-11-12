import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'models/anime.dart';
import 'services/api_service.dart';

void main() {
  runApp(const AniVexaApp());
}

class AniVexaApp extends StatelessWidget {
  const AniVexaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'AniVexa',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0F1117),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF7C4DFF),
          secondary: Color(0xFFFF6584),
          surface: Color(0xFF161922),
          onPrimary: Colors.white,
          onSurface: Colors.white,
        ),
        textTheme: GoogleFonts.poppinsTextTheme(ThemeData.dark().textTheme),
        useMaterial3: true,
      ),
      home: const MainNavigationScreen(),
    );
  }
}

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({super.key});

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _currentIndex = 0;
  List<AnimeItem> _watchlist = [];

  void _toggleWatchlist(AnimeItem anime) {
    setState(() {
      if (_watchlist.any((a) => a.id == anime.id)) {
        _watchlist.removeWhere((a) => a.id == anime.id);
      } else {
        _watchlist.add(anime);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      ScheduleView(watchlist: _watchlist, onToggleWatchlist: _toggleWatchlist),
      WatchlistView(watchlist: _watchlist, onToggleWatchlist: _toggleWatchlist),
    ];

    return Scaffold(
      body: AnimatedSwitcher(
        duration: const Duration(milliseconds: 300),
        switchInCurve: Curves.easeOutCubic,
        switchOutCurve: Curves.easeInCubic,
        child: screens[_currentIndex],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) => setState(() => _currentIndex = index),
        backgroundColor: const Color(0xFF161922),
        indicatorColor: const Color(0xFF7C4DFF).withOpacity(0.25),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.calendar_month_outlined),
            selectedIcon: Icon(Icons.calendar_month, color: Color(0xFF7C4DFF)),
            label: 'Calendario',
          ),
          NavigationDestination(
            icon: Icon(Icons.bookmark_outline),
            selectedIcon: Icon(Icons.bookmark, color: Color(0xFFFF6584)),
            label: 'Mi Lista',
          ),
        ],
      ),
    );
  }
}

class ScheduleView extends StatefulWidget {
  final List<AnimeItem> watchlist;
  final ValueChanged<AnimeItem> onToggleWatchlist;

  const ScheduleView({
    super.key,
    required this.watchlist,
    required this.onToggleWatchlist,
  });

  @override
  State<ScheduleView> createState() => _ScheduleViewState();
}

class _ScheduleViewState extends State<ScheduleView> {
  bool _isLoading = true;
  String? _errorMessage;
  List<AiringScheduleItem> _items = [];
  String _selectedTab = 'today';
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final items = _selectedTab == 'today'
          ? await ApiService.getScheduleToday()
          : await ApiService.getScheduleWeek();
      setState(() {
        _items = items;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  void _showBackendDialog() async {
    final currentUrl = await ApiService.getBackendUrl();
    final controller = TextEditingController(text: currentUrl);

    if (!mounted) return;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E222D),
        title: const Text('Configuración Backend Rust', style: TextStyle(fontWeight: FontWeight.bold)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Dirección del servicio backend AniVexa:',
              style: TextStyle(fontSize: 13, color: Colors.grey),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: controller,
              decoration: InputDecoration(
                filled: true,
                fillColor: const Color(0xFF161922),
                hintText: 'http://10.0.2.2:8080 o IP pública',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
              ),
            ),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: const Color(0xFF7C4DFF).withOpacity(0.12),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Text(
                'Nota: Si el backend local no está encendido o no responde, AniVexa se conecta automáticamente a AniList para que la app siempre funcione.',
                style: TextStyle(fontSize: 12, color: Colors.white70),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () async {
              final newUrl = controller.text.trim();
              if (newUrl.isNotEmpty) {
                await ApiService.setBackendUrl(newUrl);
                Navigator.of(ctx).pop();
                _loadData();
              }
            },
            child: const Text('Guardar y Recargar'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: const Color(0xFF7C4DFF).withOpacity(0.2),
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.play_circle_fill, color: Color(0xFF7C4DFF), size: 24),
            ),
            const SizedBox(width: 10),
            const Text(
              'AniVexa',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 22, letterSpacing: 0.5),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.dns_outlined, color: Colors.white70),
            tooltip: 'Configurar Backend Rust',
            onPressed: () => _showBackendDialog(),
          ),
          IconButton(
            icon: const Icon(Icons.refresh, color: Colors.white70),
            tooltip: 'Recargar',
            onPressed: _loadData,
          ),
        ],
        backgroundColor: const Color(0xFF0F1117),
        elevation: 0,
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                _buildTabButton('Hoy', 'today'),
                const SizedBox(width: 8),
                _buildTabButton('Esta Semana', 'week'),
              ],
            ),
          ),
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator(color: Color(0xFF7C4DFF)))
                : _errorMessage != null
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(Icons.cloud_off, size: 48, color: Colors.grey),
                            const SizedBox(height: 12),
                            Text(_errorMessage!, textAlign: TextAlign.center),
                            const SizedBox(height: 12),
                            ElevatedButton(
                              onPressed: _loadData,
                              child: const Text('Reintentar'),
                            ),
                          ],
                        ),
                      )
                    : RefreshIndicator(
                        onRefresh: _loadData,
                        color: const Color(0xFF7C4DFF),
                        child: ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: _items.length,
                          itemBuilder: (context, index) {
                            final schedule = _items[index];
                            final anime = schedule.media;
                            final inWatchlist = widget.watchlist.any((a) => a.id == anime.id);

                            return Card(
                              margin: const EdgeInsets.only(bottom: 12),
                              color: const Color(0xFF161922),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                              clipBehavior: Clip.antiAlias,
                              child: ListTile(
                                contentPadding: const EdgeInsets.all(12),
                                leading: ClipRRect(
                                  borderRadius: BorderRadius.circular(8),
                                  child: anime.imageUrl != null
                                      ? Image.network(
                                          anime.imageUrl!,
                                          width: 50,
                                          height: 70,
                                          fit: BoxFit.cover,
                                        )
                                      : Container(
                                          width: 50,
                                          height: 70,
                                          color: Colors.grey.shade800,
                                          child: const Icon(Icons.movie),
                                        ),
                                ),
                                title: Text(
                                  anime.title,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                                ),
                                subtitle: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const SizedBox(height: 4),
                                    Text(
                                      'Episodio ${schedule.episode}',
                                      style: const TextStyle(color: Color(0xFF7C4DFF), fontWeight: FontWeight.w600),
                                    ),
                                    if (anime.score != null) ...[
                                      const SizedBox(height: 2),
                                      Row(
                                        children: [
                                          const Icon(Icons.star, color: Colors.amber, size: 14),
                                          const SizedBox(width: 4),
                                          Text('${anime.score}%', style: const TextStyle(fontSize: 12)),
                                        ],
                                      ),
                                    ],
                                  ],
                                ),
                                trailing: IconButton(
                                  icon: Icon(
                                    inWatchlist ? Icons.bookmark : Icons.bookmark_border,
                                    color: inWatchlist ? const Color(0xFFFF6584) : Colors.grey,
                                  ),
                                  onPressed: () => widget.onToggleWatchlist(anime),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
          ),
        ],
      ),
    );
  }

  Widget _buildTabButton(String label, String value) {
    final isSelected = _selectedTab == value;
    return GestureDetector(
      onTap: () {
        if (_selectedTab != value) {
          setState(() => _selectedTab = value);
          _loadData();
        }
      },
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 250),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF7C4DFF) : const Color(0xFF1E222D),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.white : Colors.grey.shade400,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
          ),
        ),
      ),
    );
  }
}

class WatchlistView extends StatelessWidget {
  final List<AnimeItem> watchlist;
  final ValueChanged<AnimeItem> onToggleWatchlist;

  const WatchlistView({
    super.key,
    required this.watchlist,
    required this.onToggleWatchlist,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mi Lista de Seguimiento', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF0F1117),
      ),
      body: watchlist.isEmpty
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: const [
                  Icon(Icons.bookmark_border, size: 64, color: Colors.grey),
                  SizedBox(height: 12),
                  Text('Aún no tienes animes en tu lista', style: TextStyle(fontSize: 16, color: Colors.grey)),
                ],
              ),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: watchlist.length,
              itemBuilder: (context, index) {
                final anime = watchlist[index];
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  color: const Color(0xFF161922),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  child: ListTile(
                    leading: ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: anime.imageUrl != null
                          ? Image.network(anime.imageUrl!, width: 50, height: 70, fit: BoxFit.cover)
                          : Container(width: 50, height: 70, color: Colors.grey),
                    ),
                    title: Text(anime.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                    subtitle: Text('${anime.genres.join(", ")}'),
                    trailing: IconButton(
                      icon: const Icon(Icons.delete_outline, color: Colors.redAccent),
                      onPressed: () => onToggleWatchlist(anime),
                    ),
                  ),
                );
              },
            ),
    );
  }
}
